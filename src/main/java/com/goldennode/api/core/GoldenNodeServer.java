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
import java.net.UnknownHostException;
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

public class GoldenNodeServer extends Server {
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoldenNodeServer.class);

	private static final long serialVersionUID = 1L;
	transient private MulticastSocket multicastSocket;
	transient private DatagramSocket unicastSocket;
	transient private ServerSocket tcpSocket;
	transient private Map<String, Response> htUnicastResponse;
	transient private Map<String, List<Response>> htBlockingMulticastResponse;
	transient private Map<String, Object> unicastLocks;
	transient private Map<String, Object> blockingMulticastLocks;
	transient private InetAddress MULTICAST_ADDRESS;
	transient private Thread thMulticastProcessor;
	transient private Thread thUnicastUDPProcessor;
	transient private Thread thTCPServerSocket;
	transient private int MAX_UDPPACKET_SIZE;
	transient private int RESPONSE_WAIT_TIMEOUT;
	transient private int MULTICAST_TTL;
	transient private int REQUEST_PROCESSOR_THREADPOOL_SIZE;
	transient private ExecutorService requestProcessorThreadPool;
	transient private boolean RECEIVE_SELFMULTICAST;
	transient private Set<TCPProcessor> tcpProcessors;

	private int MULTICAST_PORT;
	private int UNICAST_UDP_PORT;
	private int UNICAST_TCP_PORT;

	public GoldenNodeServer() throws ServerException {
		try {
			loadConfig();

			setId(java.util.UUID.randomUUID().toString());
			setHost(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			throw new ServerException(e);
		}
	}

	private void loadConfig() throws UnknownHostException {
		if (System.getProperty("goldennodeserver.multicastport") == null) {
			System.setProperty("goldennodeserver.multicastport", "25000");
		}
		if (System.getProperty("goldennodeserver.unicastudpport") == null) {
			System.setProperty("goldennodeserver.unicastudpport", "25002");
		}
		if (System.getProperty("goldennodeserver.unicasttcpport") == null) {
			System.setProperty("goldennodeserver.unicasttcpport", "26002");
		}
		if (System.getProperty("goldennodeserver.multicastaddress") == null) {
			System.setProperty("goldennodeserver.multicastaddress", "225.4.5.6");
		}
		if (System.getProperty("goldennodeserver.requestretry") == null) {
			System.setProperty("goldennodeserver.requestretry", "2");
		}
		if (System.getProperty("goldennodeserver.responsewaittimeout") == null) {
			System.setProperty("goldennodeserver.responsewaittimeout", "1000");
		}
		if (System.getProperty("goldennodeserver.maxudppacketsize") == null) {
			System.setProperty("goldennodeserver.maxudppacketsize", "32768");
		}
		if (System.getProperty("goldennodeserver.multicastttl") == null) {
			System.setProperty("goldennodeserver.multicastttl", "255");
		}
		if (System.getProperty("goldennodeserver.requestprocessorthreadpoolsize") == null) {
			System.setProperty("goldennodeserver.requestprocessorthreadpoolsize", "100");
		}
		if (System.getProperty("goldennodeserver.receiveselfmulticast") == null) {
			System.setProperty("goldennodeserver.receiveselfmulticast", "false");
		}

		MULTICAST_ADDRESS = InetAddress.getByName(System.getProperty("goldennodeserver.multicastaddress"));
		MULTICAST_PORT = Integer.parseInt(System.getProperty("goldennodeserver.multicastport"));
		MAX_UDPPACKET_SIZE = Integer.parseInt(System.getProperty("goldennodeserver.maxudppacketsize"));
		RESPONSE_WAIT_TIMEOUT = Integer.parseInt(System.getProperty("goldennodeserver.responsewaittimeout"));
		MULTICAST_TTL = Integer.parseInt(System.getProperty("goldennodeserver.multicastttl"));
		UNICAST_UDP_PORT = Integer.parseInt(System.getProperty("goldennodeserver.unicastudpport"));
		UNICAST_TCP_PORT = Integer.parseInt(System.getProperty("goldennodeserver.unicasttcpport"));
		REQUEST_PROCESSOR_THREADPOOL_SIZE = Integer.parseInt(System
				.getProperty("goldennodeserver.requestprocessorthreadpoolsize"));
		RECEIVE_SELFMULTICAST = Boolean.parseBoolean(System.getProperty("goldennodeserver.receiveselfmulticast"));

	}

	private void processBlockingRequest(Request r, InetAddress remoteAddress, int remotePort) throws ServerException {
		Response rs = new Response();
		rs.setRequest(r);
		rs.setServerFrom(this);
		if (isStarted()) {
			if (getProxy() != null) {
				try {
					Object s = ReflectionUtils.callMethod(getProxy(), r.getMethod(), r.getParams());
					rs.setReturnValue(s);
				} catch (Exception e) {
					rs.setReturnValue(e);
				}
			} else {
				rs.setReturnValue(new NoClientProxySetException());

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

	private void processNonBlockingRequest(Request r, InetAddress remoteAddress, int remotePort) throws ServerException {

		if (isStarted()) {
			if (getProxy() != null) {

				try {
					ReflectionUtils.callMethod(getProxy(), r.getMethod(), r.getParams());
				} catch (Exception e) {
					throw new ServerException(e);
				}

			} else {
				throw new NoClientProxySetException();
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

						&& (((Request) receivedObject).getRequestType() == RequestType.BLOCKING_MULTICAST || ((Request) receivedObject)
								.getRequestType() == RequestType.MULTICAST)

				)

				{

					if (!RECEIVE_SELFMULTICAST) {
						continue;
					}

				}
				requestProcessorThreadPool.execute(getProcessor(receivedObject, packet));

			}

			catch (SocketException e) {
				if (e.toString().contains("Socket closed")) {
					LOGGER.trace("socket closed");
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

		public TCPProcessor(Socket s) {
			this.s = s;

			th = new Thread(this);
			tcpProcessors.add(this);
			th.start();

		}

		public void stop() {
			try {
				s.close();
			} catch (Exception e) {
				LOGGER.trace("socket couldn't be closed");
			}
			th.interrupt();
		}

		@Override
		public void run() {

			try {
				ObjectInputStream inFromClient = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream outToClient = new ObjectOutputStream(s.getOutputStream());

				while (isStarted()) {

					final Object receivedObject = inFromClient.readObject();

					LOGGER.debug("Receiving " + ((Request) receivedObject).getRequestType() + " " + receivedObject);

					final Request r = (Request) receivedObject;
					Response rs = new Response();
					rs.setRequest(r);
					rs.setServerFrom(GoldenNodeServer.this);
					if (getProxy() != null) {

						try {
							Object s = ReflectionUtils.callMethod(getProxy(), r.getMethod(), r.getParams());
							rs.setReturnValue(s);
						} catch (Exception e) {
							e.printStackTrace();
							rs.setReturnValue(e);
						}

						outToClient.writeObject(rs);

					} else {

						rs.setReturnValue(new NoClientProxySetException());
					}
				}

			} catch (EOFException e) {
				LOGGER.trace("eof occured");
			} catch (SocketException e) {
				if (e.toString().contains("Socket closed") || e.toString().contains("Connection reset")) {
				} else {
					stop();
					LOGGER.error("Error occured", e);
				}
			} catch (IOException | ClassNotFoundException e) {
				stop();
				LOGGER.error("Error occured", e);

			} finally {
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

							processNonBlockingRequest((Request) receivedObject, packet.getAddress(), packet.getPort());

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
						if (((Response) receivedObject).getRequest().getRequestType() == RequestType.BLOCKING_MULTICAST) {

							Object lock = blockingMulticastLocks.get(((Response) receivedObject).getRequest().getId());
							if (lock != null) {
								htUnicastResponse.put(((Response) receivedObject).getRequest().getId(),
										(Response) receivedObject);

								List<Response> l = htBlockingMulticastResponse.get(((Response) receivedObject)
										.getRequest().getId());
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
			getServerStateListener().serverStarting(GoldenNodeServer.this);
			htUnicastResponse = new ConcurrentHashMap<String, Response>();
			htBlockingMulticastResponse = new ConcurrentHashMap<String, List<Response>>();
			unicastLocks = new ConcurrentHashMap<String, Object>();
			blockingMulticastLocks = new ConcurrentHashMap<String, Object>();
			tcpProcessors = new HashSet<TCPProcessor>();
			getMulticastSocket();
			getUnicastSocket();
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
							TCPProcessor tc = new TCPProcessor(s);
							tcpProcessors.add(tc);
						}

					} catch (SocketException e) {
						if (e.toString().contains("Socket closed")) {
							LOGGER.trace("socket closed");
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
			getServerStateListener().serverStarted(GoldenNodeServer.this);

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
			getServerStateListener().serverStopping(GoldenNodeServer.this);
			setStarted(false);
			multicastSocket.close();
			unicastSocket.close();
			tcpSocket.close();
			thMulticastProcessor.interrupt();
			thUnicastUDPProcessor.interrupt();
			thTCPServerSocket.interrupt();
			requestProcessorThreadPool.shutdown();
			getServerStateListener().serverStopped(GoldenNodeServer.this);
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
	public Request prepareRequest(String method, Object... params) {
		Request r = new Request();
		r.setMethod(method);
		r.addParams(params);
		r.setServerFrom(this);
		return r;
	}

	@Override
	public Response unicastTCP(Server remoteServer, Request request) throws ServerException {
		Socket clientSocket = null;
		ObjectOutputStream outToServer = null;
		ObjectInputStream inFromServer = null;
		try {
			if (isStarted()) {
				request.setRequestType(RequestType.UNICAST_TCP);
				LOGGER.debug("Sending " + request.getRequestType() + " " + request);
				clientSocket = new Socket(remoteServer.getHost(), remoteServer.getUnicastTCPPort());

				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());

				outToServer.writeObject(request);

				Response response = (Response) inFromServer.readObject();
				if (response.getReturnValue() instanceof Exception) {
					throw new ServerException((Exception) response.getReturnValue());
				}
				return response;

			} else {
				throw new ServerNotStartedException();
			}
		} catch (ClassNotFoundException | IOException e) {
			throw new ServerException(e);
		} finally {

			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) { //
				LOGGER.trace("socket couldn't be closed");
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
						lock.wait(RESPONSE_WAIT_TIMEOUT);
					} catch (InterruptedException e) {
						LOGGER.trace("interruption");
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
	public List<Response> blockingMulticast(Request request, Long timeout) throws ServerException {
		return blockingMulticast(request, timeout, -1, false, false);
	}

	public List<Response> blockingMulticast(Request request, Long timeout, int maxResponses,
			boolean throwExceptionIfFewerResponses, boolean failForPartialFailures) throws ServerException {

		try {
			if (isStarted()) {
				request.setRequestType(RequestType.BLOCKING_MULTICAST);
				LOGGER.debug("Sending " + request.getRequestType() + " " + request);
				byte[] bytes = request.getBytes();
				if (bytes.length > MAX_UDPPACKET_SIZE) {
					throw new PacketSizeExceededException();
				}
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length, MULTICAST_ADDRESS, MULTICAST_PORT);

				Object lock = new Object();
				blockingMulticastLocks.put(request.getId(), lock);
				htBlockingMulticastResponse.put(request.getId(),
						Collections.synchronizedList(new FixedSizeList<Response>(maxResponses)));
				multicastSocket.send(packet);
				synchronized (lock) {
					try {
						lock.wait(timeout);
					} catch (InterruptedException e) {
						LOGGER.trace("interruption");
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
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length, MULTICAST_ADDRESS, MULTICAST_PORT);
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

	private void getUnicastSocket() throws IOException {
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

	private void getMulticastSocket() throws IOException {

		multicastSocket = new MulticastSocket(null);
		multicastSocket.setTimeToLive(MULTICAST_TTL);
		multicastSocket.setBroadcast(true);
		multicastSocket.joinGroup(MULTICAST_ADDRESS);
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
