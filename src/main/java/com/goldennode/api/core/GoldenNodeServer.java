package com.goldennode.api.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.ReflectionUtils;
import com.goldennode.api.helper.SystemUtils;

public class GoldenNodeServer extends Server {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeServer.class);
	private static final long serialVersionUID = 1L;
	private transient MulticastSocket multicastSocket;
	private transient DatagramSocket unicastSocket;
	private transient ServerSocket tcpSocket;
	private transient Map<String, Response> htUnicastResponse;
	private transient Map<String, List<Response>> htBlockingMulticastResponse;
	private transient Map<String, Object> unicastLocks;
	private transient Map<String, Object> blockingMulticastLocks;
	private transient Thread thMulticastProcessor;
	private transient Thread thUnicastUDPProcessor;
	private transient Thread thTCPServerSocket;
	private transient Set<TCPProcessor> tcpProcessors;
	private transient ExecutorService requestProcessorThreadPool;
	private transient int MAX_UDPPACKET_SIZE = Integer.parseInt(
			SystemUtils.getSystemProperty("32768", "com.goldennode.api.core.GoldenNodeServer.maxUDPPacketSize"));
	private transient int MULTICAST_TTL = Integer
			.parseInt(SystemUtils.getSystemProperty("255", "com.goldennode.api.core.GoldenNodeServer.multicastTTL"));
	private transient int REQUEST_PROCESSOR_THREADPOOL_SIZE = Integer.parseInt(SystemUtils.getSystemProperty("100",
			"com.goldennode.api.core.GoldenNodeServer.requestProcessorThreadpoolSize"));
	private transient boolean RECEIVE_SELFMULTICAST = Boolean.parseBoolean(
			SystemUtils.getSystemProperty("false", "com.goldennode.api.core.GoldenNodeServer.receiveSelfMulticast"));
	private transient String MULTICAST_ADDRESS = SystemUtils.getSystemProperty("225.4.5.6", // NOPMD
			"com.goldennode.api.core.GoldenNodeServer.multicastAddress");
	private int MULTICAST_PORT = Integer
			.parseInt(SystemUtils.getSystemProperty("25000", "com.goldennode.api.core.GoldenNodeServer.multicastPort"));
	private int UNICAST_UDP_PORT = Integer.parseInt(
			SystemUtils.getSystemProperty("25002", "com.goldennode.api.core.GoldenNodeServer.unicastUDPPort"));
	private int UNICAST_TCP_PORT = Integer.parseInt(
			SystemUtils.getSystemProperty("26002", "com.goldennode.api.core.GoldenNodeServer.unicastTCPPort"));

	public GoldenNodeServer(String serverId) throws ServerException {
		super(serverId);
	}

	public GoldenNodeServer() throws ServerException {
		super(null);
	}

	private void processBlockingRequest(Request r, InetAddress remoteAddress, int remotePort) throws ServerException {
		Response rs = new Response();
		rs.setRequest(r);
		rs.setServerFrom(this);
		if (isStarted()) {
			if (getOperationBase() != null) {
				try {
					Object s = ReflectionUtils.callMethod(getOperationBase(), r.getMethod(), r.getParams());
					rs.setReturnValue(s);
				} catch (Exception e) {
					rs.setReturnValue(e);
				}
			} else {
				rs.setReturnValue(new NoOperationBaseException());
			}
		} else {
			rs.setReturnValue(new ServerNotStartedException());
		}
		multicastSender(rs, remoteAddress, remotePort);
	}

	private void multicastSender(Response rs, InetAddress remoteAddress, int remotePort) throws ServerException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream gos = new ObjectOutputStream(bos);
			gos.writeObject(rs);
			gos.close();
			byte[] bytes = bos.toByteArray();
			if (bytes.length > MAX_UDPPACKET_SIZE) {
				throw new PacketSizeExceededException();
			}
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteAddress, remotePort);
			multicastSocket.send(packet);
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	private void processNonBlockingRequest(Request r) throws ServerException {
		if (isStarted()) {
			if (getOperationBase() != null) {
				try {
					ReflectionUtils.callMethod(getOperationBase(), r.getMethod(), r.getParams());
				} catch (Exception e) {
					throw new ServerException(e);
				}
			} else {
				throw new NoOperationBaseException();
			}
		} else {
			throw new ServerNotStartedException();
		}
	}

	@Override
	public int getMulticastPort() {
		return MULTICAST_PORT;
	}

	public void processUDPRequests(DatagramSocket socket) {
		while (isStarted()) {
			try {
				byte[] buf = new byte[MAX_UDPPACKET_SIZE];
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				ByteArrayInputStream bis = new ByteArrayInputStream(buf);
				ObjectInputStream gis = new ObjectInputStream(bis);
				final Object receivedObject = gis.readObject();
				if (receivedObject instanceof Request
						&& ((Request) receivedObject).getServerFrom().equals(GoldenNodeServer.this)
						&& (((Request) receivedObject).getRequestType() == RequestType.BLOCKING_MULTICAST
								|| ((Request) receivedObject).getRequestType() == RequestType.MULTICAST)) {
					if (!RECEIVE_SELFMULTICAST) {// NOPMD
						continue;
					}
				}
				if (receivedObject instanceof Request) {
					processId.set(((Request) receivedObject).getProcessId());
				}
				requestProcessorThreadPool.execute(getProcessor(receivedObject, packet));
			} catch (SocketException e) {
				if (e.toString().contains("Socket closed")) {
					// LOGGER.trace("socket closed");
				} else {
					LOGGER.error("Error occured", e);
				}
			} catch (IOException | ClassNotFoundException e) {
				LOGGER.error("Error occured", e);
			}
		}
	}

	public class TCPProcessor implements Runnable {
		private Socket s;
		private Thread th;

		public TCPProcessor(Socket s, String serverId) {
			this.s = s;
			th = new Thread(this, "TCPProcessor " + serverId);
			tcpProcessors.add(this);
			th.start();
		}

		public void stop() {
			try {
				s.close();
			} catch (Exception e) {
				// LOGGER.trace("socket couldn't be closed");
			}
			th.interrupt();
		}

		@Override
		public void run() {
			Request r = null;
			try {
				ObjectInputStream inFromClient = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream outToClient = new ObjectOutputStream(s.getOutputStream());
				while (isStarted()) {
					final Object receivedObject = inFromClient.readObject();
					LOGGER.debug("Receiving " + ((Request) receivedObject).getRequestType() + " " + receivedObject);
					r = (Request) receivedObject;
					processId.set(r.getProcessId());
					Response rs = new Response();
					rs.setRequest(r);
					rs.setServerFrom(GoldenNodeServer.this);
					if (getOperationBase() != null) {
						try {
							Object s = ReflectionUtils.callMethod(getOperationBase(), r.getMethod(), r.getParams());
							rs.setReturnValue(s);
						} catch (Exception e) {
							rs.setReturnValue(e);
						}
						outToClient.writeObject(rs);
					} else {
						rs.setReturnValue(new NoOperationBaseException());
					}
				}
			} catch (EOFException e) {
				// LOGGER.trace("eof occured");
			} catch (SocketException e) {
				if (e.toString().contains("Socket closed") || e.toString().contains("Connection reset")
						|| e.toString().contains("Broken pipe")) {// NOPMD
					// Don't do anything
				} else {
					// stop();
					LOGGER.error("Error occured" + (r == null ? "" : " while processing " + r) + " ", e.toString());
				}
			} catch (IOException | ClassNotFoundException e) {
				// stop();
				LOGGER.error("Error occured" + (r == null ? "" : " while processing " + r) + " ", e.toString());
			} finally {
				stop();
				tcpProcessors.remove(this);
			}
		}
	}

	private Runnable getProcessor(final Object receivedObject, final DatagramPacket packet) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					if (receivedObject instanceof Request) {
						LOGGER.debug("Receiving " + ((Request) receivedObject).getRequestType() + " " + receivedObject);
						if (((Request) receivedObject).getRequestType() == RequestType.BLOCKING_MULTICAST
								|| ((Request) receivedObject).getRequestType() == RequestType.UNICAST_UDP) {
							processBlockingRequest((Request) receivedObject, packet.getAddress(), packet.getPort());
						}
						if (((Request) receivedObject).getRequestType() == RequestType.MULTICAST) {
							processNonBlockingRequest((Request) receivedObject);
						}
					}
					if (receivedObject instanceof Response) {
						LOGGER.debug("Receiving " + ((Response) receivedObject).getRequest().getRequestType() + " "
								+ receivedObject);
						if (((Response) receivedObject).getRequest().getRequestType() == RequestType.UNICAST_UDP) {
							Object lock = unicastLocks.get(((Response) receivedObject).getRequest().getId());
							if (lock != null) {
								htUnicastResponse.put(((Response) receivedObject).getRequest().getId(),
										(Response) receivedObject);
								synchronized (lock) {
									lock.notifyAll();
								}
							} else {
								LOGGER.debug("Ignoring " + ((Response) receivedObject).getRequest().getRequestType()
										+ " " + receivedObject);
							}
						}
						if (((Response) receivedObject).getRequest()
								.getRequestType() == RequestType.BLOCKING_MULTICAST) {
							Object lock = blockingMulticastLocks.get(((Response) receivedObject).getRequest().getId());
							if (lock != null) {
								htUnicastResponse.put(((Response) receivedObject).getRequest().getId(),
										(Response) receivedObject);
								List<Response> l = htBlockingMulticastResponse
										.get(((Response) receivedObject).getRequest().getId());
								if (l != null) {
									boolean b = l.add((Response) receivedObject);
									if (!b) {// list is full notify
										synchronized (lock) {
											lock.notifyAll();
										}
									}
								}
							} else {
								LOGGER.debug("Ignoring " + ((Response) receivedObject).getRequest().getRequestType()
										+ " " + receivedObject);
							}
						}
					}
				} catch (ServerException e) {
					LOGGER.error("Error occured", e);
				}
			}
		};
	}

	@Override
	public void start() throws ServerException {
		try {
			if (isStarted()) {
				throw new ServerAlreadyStartedException();
			}
			for (ServerStateListener listener : getServerStateListeners()) {
				listener.serverStarting(GoldenNodeServer.this);
			}
			htUnicastResponse = new ConcurrentHashMap<String, Response>();
			htBlockingMulticastResponse = new ConcurrentHashMap<String, List<Response>>();
			unicastLocks = new ConcurrentHashMap<String, Object>();
			blockingMulticastLocks = new ConcurrentHashMap<String, Object>();
			tcpProcessors = new HashSet<TCPProcessor>();
			createMulticastSocket();
			createUnicastSocket();
			getTcpServerSocket();
			requestProcessorThreadPool = Executors.newFixedThreadPool(REQUEST_PROCESSOR_THREADPOOL_SIZE);
			thMulticastProcessor = new Thread(new Runnable() {
				@Override
				public void run() {
					processUDPRequests(multicastSocket);
				}
			});
			thUnicastUDPProcessor = new Thread(new Runnable() {
				@Override
				public void run() {
					processUDPRequests(unicastSocket);
				}
			});
			thTCPServerSocket = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (isStarted()) {
							Socket s = tcpSocket.accept();
							TCPProcessor tc = new TCPProcessor(s, GoldenNodeServer.this.getShortId());
							tcpProcessors.add(tc);
						}
					} catch (SocketException e) {
						if (e.toString().contains("Socket closed")) {
							// LOGGER.trace("socket closed");
						} else {
							LOGGER.error("Error occured", e);
						}
					} catch (IOException e) {
						LOGGER.error("Error occured", e);
					}
				}
			});
			setStarted(true);
			thMulticastProcessor.start();
			thUnicastUDPProcessor.start();
			thTCPServerSocket.start();
			LOGGER.debug("Server listening to unicastudpport:" + UNICAST_UDP_PORT + " multicastport:" + MULTICAST_PORT
					+ " unicasttcpport:" + UNICAST_TCP_PORT);
			for (ServerStateListener listener : getServerStateListeners()) {
				listener.serverStarted(GoldenNodeServer.this);
			}
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	@Override
	public void stop() throws ServerException {
		try {
			if (!isStarted()) {
				throw new ServerAlreadyStoppedException();
			}
			for (ServerStateListener listener : getServerStateListeners()) {
				listener.serverStopping(GoldenNodeServer.this);
			}
			setStarted(false);
			multicastSocket.close();
			unicastSocket.close();
			tcpSocket.close();
			thMulticastProcessor.interrupt();
			thUnicastUDPProcessor.interrupt();
			thTCPServerSocket.interrupt();
			try {
				thMulticastProcessor.join();
				thUnicastUDPProcessor.join();
				thTCPServerSocket.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			requestProcessorThreadPool.shutdown();
			for (ServerStateListener listener : getServerStateListeners()) {
				listener.serverStopped(GoldenNodeServer.this);
			}
			Iterator<TCPProcessor> iter = tcpProcessors.iterator();
			while (iter.hasNext()) {
				TCPProcessor s = iter.next();
				s.stop();
			}
			tcpProcessors.clear();
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	@Override
	public Request prepareRequest(String method, RequestOptions options, Object... params) {
		Request r = new Request();
		r.setMethod(method);
		r.addParams(params);
		r.setServerFrom(this);
		r.setTimeout(options.getTimeout());
		r.setProcessId(createProcessId());
		return r;
	}

	@Override
	public Response unicastTCP(Server remoteServer, Request request) throws ServerException {
		// int retry = 0;
		// ServerException ex = null;
		// while (retry++ < 3) {
		// try {
		return doUnicastTCP(remoteServer, request);
		// } catch (ServerException e) {
		// ex = e;
		// }
		// }
		// throw ex;
	}

	private Response doUnicastTCP(Server remoteServer, Request request) throws ServerException {
		Socket clientSocket = null;
		ObjectOutputStream outToServer = null;
		ObjectInputStream inFromServer = null;
		try {
			if (isStarted()) {
				request.setRequestType(RequestType.UNICAST_TCP);
				LOGGER.debug("Sending " + request.getRequestType() + " " + request);
				clientSocket = new Socket(remoteServer.getHost(), remoteServer.getUnicastTCPPort());
				clientSocket.setSoTimeout(request.getTimeout());
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());
				outToServer.writeObject(request);
				Response response = (Response) inFromServer.readObject();
				LOGGER.debug("Received " + request.getRequestType() + " " + response);
				if (response.getReturnValue() instanceof Exception) {
					throw new ServerException((Exception) response.getReturnValue());
				}
				return response;
			} else {
				throw new ServerNotStartedException();
			}
		} catch (SocketTimeoutException e) {
			throw new ServerException(
					"cant execute request " + request + " on server " + remoteServer + " " + e.toString());
		} catch (ClassNotFoundException | IOException e) {
			throw new ServerException(
					"cant execute request " + request + " on server " + remoteServer + " " + e.toString());
		} finally {
			try {
				if (inFromServer != null) {
					inFromServer.close();
				}
				if (outToServer != null) {
					outToServer.close();
				}
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) { //
				// LOGGER.trace("socket couldn't be closed");
			}
		}
	}

	@Override
	public Response unicastUDP(Server remoteServer, Request request) throws ServerException {
		try {
			if (isStarted()) {
				request.setRequestType(RequestType.UNICAST_UDP);
				LOGGER.debug("Sending " + request.getRequestType() + " " + request);
				byte[] bytes = request.getBytes();
				if (bytes.length > MAX_UDPPACKET_SIZE) {
					throw new PacketSizeExceededException();
				}
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteServer.getHost(),
						remoteServer.getUnicastUDPPort());
				Object lock = new Object();
				unicastLocks.put(request.getId(), lock);
				unicastSocket.send(packet);
				synchronized (lock) {
					try {
						lock.wait(request.getTimeout());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				unicastLocks.remove(request.getId());
				Response resp = htUnicastResponse.remove(request.getId());
				if (resp == null) {
					throw new ServerCallTimeoutException("ServerCallTimeoutException. Request:" + request.toString());
				}
				if (resp.getReturnValue() instanceof Exception) {
					throw new ServerException((Exception) resp.getReturnValue());
				}
				return resp;
			} else {
				throw new ServerNotStartedException();
			}
		} catch (ServerCallTimeoutException e) {
			throw e;
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	@Override
	public List<Response> blockingMulticast(Request request) throws ServerException {
		return blockingMulticast(request, -1, false, false);
	}

	public List<Response> blockingMulticast(Request request, int maxResponses, boolean throwExceptionIfFewerResponses, boolean failForPartialFailures)
			throws ServerException {
		try {
			if (isStarted()) {
				request.setRequestType(RequestType.BLOCKING_MULTICAST);
				LOGGER.debug("Sending " + request.getRequestType() + " " + request);
				byte[] bytes = request.getBytes();
				if (bytes.length > MAX_UDPPACKET_SIZE) {
					throw new PacketSizeExceededException();
				}
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
						InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
				Object lock = new Object();
				blockingMulticastLocks.put(request.getId(), lock);
				htBlockingMulticastResponse.put(request.getId(),
						Collections.synchronizedList(new FixedSizeList<Response>(maxResponses)));
				multicastSocket.send(packet);
				synchronized (lock) {
					try {
						lock.wait(request.getTimeout());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				blockingMulticastLocks.remove(request.getId());
				List<Response> resp = htBlockingMulticastResponse.remove(request.getId());
				if (resp.size() < maxResponses && throwExceptionIfFewerResponses) {
					throw new ServerCallTimeoutException("ServerCallTimeoutException. Request:" + request.toString());
				}
				if (failForPartialFailures) {
					Iterator<Response> iter = resp.iterator();
					while (iter.hasNext()) {
						Response res = iter.next();
						if (res.getReturnValue() instanceof Exception) {
							throw new ServerException((Exception) res.getReturnValue());
						}
					}
				}
				return resp;
			} else {
				throw new ServerNotStartedException();
			}
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	@Override
	public void multicast(Request request) throws ServerException {
		try {
			if (isStarted()) {
				request.setRequestType(RequestType.MULTICAST);
				LOGGER.debug("Sending " + request.getRequestType() + " " + request);
				byte[] bytes = request.getBytes();
				if (bytes.length > MAX_UDPPACKET_SIZE) {
					throw new PacketSizeExceededException();
				}
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
						InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
				multicastSocket.send(packet);
			} else {
				throw new ServerNotStartedException();
			}
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	private void getTcpServerSocket() throws IOException {
		int i = 0;
		while (i < 1000) {
			try {
				UNICAST_TCP_PORT = UNICAST_TCP_PORT + i;
				tcpSocket = new ServerSocket(UNICAST_TCP_PORT);
				return;
			} catch (IOException e) {
				i++;
			}
		}
		throw new IOException("Can not bind to any tcp port");
	}

	private void createUnicastSocket() throws IOException {
		int i = 0;
		while (i < 1000) {
			try {
				unicastSocket = new DatagramSocket(null);
				UNICAST_UDP_PORT = UNICAST_UDP_PORT + i;
				unicastSocket.bind(new InetSocketAddress(UNICAST_UDP_PORT));
				unicastSocket.setReuseAddress(false);
				return;
			} catch (IOException e) {
				i++;
			}
		}
		throw new IOException("Can not bind to any udp port");
	}

	private void createMulticastSocket() throws IOException {
		multicastSocket = new MulticastSocket(null);
		multicastSocket.setTimeToLive(MULTICAST_TTL);
		multicastSocket.setBroadcast(true);
		multicastSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
		multicastSocket.setReuseAddress(true);
		multicastSocket.bind(new InetSocketAddress(MULTICAST_PORT));
	}

	@Override
	public int getUnicastUDPPort() {
		return UNICAST_UDP_PORT;
	}

	@Override
	public int getUnicastTCPPort() {
		return UNICAST_TCP_PORT;
	}

}
