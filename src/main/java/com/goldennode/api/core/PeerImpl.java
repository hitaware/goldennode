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

import org.slf4j.LoggerFactory;

import com.goldennode.api.helper.LockHelper;
import com.goldennode.api.helper.ReflectionUtils;
import com.goldennode.api.helper.StringUtils;
import com.goldennode.api.helper.SystemUtils;

public class PeerImpl extends Peer {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PeerImpl.class);
    private static final long serialVersionUID = 1L;
    private transient MulticastSocket multicastSocket;
    private transient DatagramSocket unicastSocket;
    private transient ServerSocket tcpServerSocket;
    private transient Map<String, Response> htUnicastResponse;
    private transient Map<String, List<Response>> htBlockingMulticastResponse;
    private transient Map<String, Object> unicastLocks;
    private transient volatile Map<String, Object> blockingMulticastLocks;
    private transient Thread thMulticastProcessor;
    private transient Thread thUnicastUDPProcessor;
    private transient Thread thTCPPeerSocket;
    private transient Set<TCPProcessor> tcpProcessors;
    private transient Set<UDPProcessor> udpProcessors;
    private transient int MAX_UDPPACKET_SIZE = Integer.parseInt(
            SystemUtils.getSystemProperty("32768", "com.goldennode.api.core.GoldenNodePeer.maxUDPPacketSize"));
    private transient int MULTICAST_TTL = Integer
            .parseInt(SystemUtils.getSystemProperty("255", "com.goldennode.api.core.GoldenNodePeer.multicastTTL"));
    private transient boolean RECEIVE_SELFMULTICAST = Boolean.parseBoolean(
            SystemUtils.getSystemProperty("false", "com.goldennode.api.core.GoldenNodePeer.receiveSelfMulticast"));
    private transient String MULTICAST_ADDRESS = SystemUtils.getSystemProperty("225.4.5.6", // NOPMD
            "com.goldennode.api.core.GoldenNodePeer.multicastAddress");
    private int MULTICAST_PORT = Integer
            .parseInt(SystemUtils.getSystemProperty("27000", "com.goldennode.api.core.GoldenNodePeer.multicastPort"));
    private int UNICAST_UDP_PORT = Integer
            .parseInt(SystemUtils.getSystemProperty("26002", "com.goldennode.api.core.GoldenNodePeer.unicastUDPPort"));
    private int UNICAST_TCP_PORT = Integer
            .parseInt(SystemUtils.getSystemProperty("25002", "com.goldennode.api.core.GoldenNodePeer.unicastTCPPort"));

    public PeerImpl(String peerId, int multicastPort) throws PeerException {
        super(peerId);
        MULTICAST_PORT = multicastPort;
    }

    public PeerImpl(int multicastPort) throws PeerException {
        super();
        MULTICAST_PORT = multicastPort;
    }

    public PeerImpl(String peerId) throws PeerException {
        super(peerId);
    }

    public PeerImpl() throws PeerException {
        super();
    }

    long udpProcessorThreadCounter = 1;
    long tcpProcessorThreadCounter = 1;

    synchronized long getUDPProcessorThreadCounter() {
        return udpProcessorThreadCounter++;
    }

    synchronized long getTCPProcessorThreadCounter() {
        return tcpProcessorThreadCounter++;
    }

    @SuppressWarnings("PMD")
    private void processBlockingRequest(Request r, InetAddress remoteAddress, int remotePort) throws PeerException {
        Response rs = new Response();
        rs.setRequest(r);
        rs.setPeerFrom(this);
        if (isStarted()) {
            if (getOperationBase() != null) {
                try {
                    Object s = ReflectionUtils.callMethod(getOperationBase(), r.getMethod(), r.getParams());
                    rs.setReturnValue(s);
                } catch (Exception e) {
                    LOGGER.error("Error occured on blocking request", e);
                    rs.setReturnValue(e);
                }
            } else {
                rs.setReturnValue(new NoOperationBaseException());
            }
        } else {
            rs.setReturnValue(new PeerNotStartedException());
        }
        respondToRequest(rs, remoteAddress, remotePort);
    }

    void respondToRequest(Response rs, InetAddress remoteAddress, int remotePort) throws PeerException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream gos = new ObjectOutputStream(bos);
            gos.writeObject(rs);
            gos.close();
            byte[] bytes = bos.toByteArray();
            if (bytes.length > MAX_UDPPACKET_SIZE) {// TODO do we need?Yes
                throw new PacketSizeExceededException();
            }
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteAddress, remotePort);
            unicastSocket.send(packet);
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    @SuppressWarnings("PMD")
    private void processNonBlockingRequest(Request r) throws PeerException {
        if (isStarted()) {
            if (getOperationBase() != null) {
                try {
                    ReflectionUtils.callMethod(getOperationBase(), r.getMethod(), r.getParams());
                } catch (Exception e) {
                    LOGGER.error("Error occured on non-blocking request", e);
                    throw new PeerException(e);
                }
            } else {
                throw new NoOperationBaseException();
            }
        } else {
            throw new PeerNotStartedException();
        }
    }

    @Override
    public int getMulticastPort() {
        return MULTICAST_PORT;
    }

    public void processUDPRequests(DatagramSocket socket) {
        try {
            while (isStarted()) {
                byte[] buf = new byte[MAX_UDPPACKET_SIZE];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                ByteArrayInputStream bis = new ByteArrayInputStream(buf);
                ObjectInputStream gis = new ObjectInputStream(bis);
                final Object receivedObject = gis.readObject();
                if (receivedObject instanceof Request && ((Request) receivedObject).getPeerFrom().equals(this)
                        && (((Request) receivedObject).getRequestType() == RequestType.BLOCKING_MULTICAST
                                || ((Request) receivedObject).getRequestType() == RequestType.MULTICAST)) {
                    if (!RECEIVE_SELFMULTICAST) {// NOPMD
                        continue;
                    }
                }
                if (receivedObject instanceof Request) {
                    Peer.processId.set(((Request) receivedObject).getProcessId());
                }
                UDPProcessor udpProcessor = new UDPProcessor(socket, receivedObject);
                udpProcessor.start();
            }
        } catch (SocketException e) {
            if (e.toString().contains("Socket closed")) {//NOPMD
                //LOGGER.trace("socket closed");
            } else {
                LOGGER.error("Error occured", e);
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error occured", e);
        }
    }

    public class TCPProcessor implements Runnable {
        private Socket socket;
        private Thread th;

        public TCPProcessor(Socket socket, String peerId) {
            this.socket = socket;
            th = new Thread(this, StringUtils.shortId(peerId) + " TCPProcessor " + getTCPProcessorThreadCounter());
            tcpProcessors.add(this);
        }

        public void start() {
            th.start();
        }

        public void stop() {
            try {
                socket.close();
            } catch (Exception e) {//NOPMD
                LOGGER.trace("socket couldn't be closed");
            }
            try {
                th.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run() {
            Request r = null;
            try {
                ObjectInputStream inFromClient = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outToClient = new ObjectOutputStream(socket.getOutputStream());
                while (isStarted()) {
                    final Object receivedObject = inFromClient.readObject();
                    //LOGGER.trace("Receiving " + ((Request) receivedObject).getRequestType() + " " + receivedObject);
                    r = (Request) receivedObject;
                    Peer.processId.set(r.getProcessId());
                    Response rs = new Response();
                    rs.setRequest(r);
                    rs.setPeerFrom(PeerImpl.this);
                    if (getOperationBase() != null) {
                        try {
                            Object s = ReflectionUtils.callMethod(getOperationBase(), r.getMethod(), r.getParams());
                            rs.setReturnValue(s);
                        } catch (Exception e) {
                            LOGGER.error("Error occured on tcp request", e);
                            rs.setReturnValue(e);
                        }
                        outToClient.writeObject(rs);
                    } else {
                        rs.setReturnValue(new NoOperationBaseException());
                    }
                }
            } catch (EOFException e) {//NOPMD
                //LOGGER.trace("eof occured");
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
                tcpProcessors.remove(this);
            }
        }
    }

    class UDPProcessor implements Runnable {
        private Object receivedObject;
        private Thread th;
        private DatagramSocket socket;

        UDPProcessor(final DatagramSocket socket, final Object receivedObject) {
            this.receivedObject = receivedObject;
            this.socket = socket;
            th = new Thread(this, StringUtils.shortId(getId()) + " UDPProcessor " + getUDPProcessorThreadCounter());
            udpProcessors.add(this);
        }

        public void start() {
            th.start();
        }

        public void stop() {
            th.interrupt();
            try {
                LOGGER.debug("Will join. Peer" + StringUtils.shortId(getId()) + " Thread:"
                        + Thread.currentThread().getName());
                th.join();
                LOGGER.debug(
                        "Joined. Peer:" + StringUtils.shortId(getId()) + " Thread:" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run() {
            try {
                if (receivedObject instanceof Request) {
                    LOGGER.debug("Receiving " + ((Request) receivedObject).getRequestType() + " " + receivedObject);
                    if (((Request) receivedObject).getRequestType() == RequestType.BLOCKING_MULTICAST
                            || ((Request) receivedObject).getRequestType() == RequestType.UNICAST_UDP) {
                        processBlockingRequest((Request) receivedObject,
                                ((Request) receivedObject).getPeerFrom().getHost(),
                                ((Request) receivedObject).getPeerFrom().getUnicastUDPPort());
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
                            LOGGER.debug("will notify. Peer:" + StringUtils.shortId(getId()) + " Thread:"
                                    + Thread.currentThread().getName());
                            synchronized (lock) {
                                lock.notifyAll();
                                LOGGER.debug("notified. Peer" + StringUtils.shortId(getId()) + " Thread:"
                                        + Thread.currentThread().getName());
                            }
                        } else {
                            LOGGER.error("Ignoring " + ((Response) receivedObject).getRequest().getRequestType() + " "
                                    + receivedObject);
                        }
                    }
                    if (((Response) receivedObject).getRequest().getRequestType() == RequestType.BLOCKING_MULTICAST) {
                        Object lock = blockingMulticastLocks.get(((Response) receivedObject).getRequest().getId());
                        if (lock != null) {
                            List<Response> l = htBlockingMulticastResponse
                                    .get(((Response) receivedObject).getRequest().getId());
                            if (l != null) {
                                boolean b = l.add((Response) receivedObject);
                                if (!b) {// list is full notify
                                    LOGGER.debug("will notify. Peer:" + StringUtils.shortId(getId()) + " Thread:"
                                            + Thread.currentThread().getName());
                                    synchronized (lock) {
                                        lock.notifyAll();
                                        LOGGER.debug("notified. Peer" + StringUtils.shortId(getId()) + " Thread:"
                                                + Thread.currentThread().getName());
                                    }
                                }
                            }
                        } else {
                            LOGGER.error("Ignoring " + ((Response) receivedObject).getRequest().getRequestType() + " "
                                    + receivedObject);
                        }
                    }
                }
            } catch (PeerException e) {
                LOGGER.error("Error occured", e);
            } finally {
                udpProcessors.remove(this);
            }
        }
    }

    @Override
    public void start(int delay) throws PeerException {
        try {
            LockHelper.sleep(delay);
            if (isStarted()) {
                throw new PeerAlreadyStartedException();
            }
            for (PeerStateListener listener : getPeerStateListeners()) {
                listener.peerStarting(this);
            }
            htUnicastResponse = new ConcurrentHashMap<String, Response>();
            htBlockingMulticastResponse = new ConcurrentHashMap<String, List<Response>>();
            unicastLocks = new ConcurrentHashMap<String, Object>();
            blockingMulticastLocks = new ConcurrentHashMap<String, Object>();
            tcpProcessors = Collections.synchronizedSet(new HashSet<>());
            udpProcessors = Collections.synchronizedSet(new HashSet<>());
            createMulticastSocket();
            createUnicastSocket();
            getTcpPeerSocket();
            thMulticastProcessor = new Thread(new Runnable() {
                @Override
                public void run() {
                    processUDPRequests(multicastSocket);
                }
            }, StringUtils.shortId(getId()) + " multicastProcessor");
            thUnicastUDPProcessor = new Thread(new Runnable() {
                @Override
                public void run() {
                    processUDPRequests(unicastSocket);
                }
            }, StringUtils.shortId(getId()) + " UnicastUDPProcessor");
            thTCPPeerSocket = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (isStarted()) {
                            Socket s = tcpServerSocket.accept();
                            TCPProcessor tc = new TCPProcessor(s, getId());
                            tc.start();
                        }
                    } catch (SocketException e1) {
                        if (e1.toString().contains("Socket closed")) {//NOPMD
                            //LOGGER.trace("socket closed");
                        } else {
                            LOGGER.error("Error occured", e1);
                        }
                    } catch (IOException e2) {
                        LOGGER.error("Error occured", e2);
                    }
                }
            }, StringUtils.shortId(getId()) + " TCPServerSocket");
            setStarted(true);
            thMulticastProcessor.start();
            thUnicastUDPProcessor.start();
            thTCPPeerSocket.start();
            LOGGER.debug("Peer listening to unicastudpport:" + UNICAST_UDP_PORT + " multicastport:" + MULTICAST_PORT
                    + " unicasttcpport:" + UNICAST_TCP_PORT);
            for (PeerStateListener listener : getPeerStateListeners()) {
                listener.peerStarted(this);
            }
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    @Override
    public void stop(int delay) throws PeerException {
        try {
            LockHelper.sleep(delay);
            if (!isStarted()) {
                throw new PeerAlreadyStoppedException();
            }
            for (PeerStateListener listener : getPeerStateListeners()) {
                listener.peerStopping(this);
            }
            setStarted(false);
            multicastSocket.close();
            while (!multicastSocket.isClosed()) {
                LockHelper.sleep(1000);
            }
            unicastSocket.close();
            while (!unicastSocket.isClosed()) {
                LockHelper.sleep(1000);
            }
            tcpServerSocket.close();
            try {
                thMulticastProcessor.join();
            } catch (InterruptedException e) {
                LOGGER.error("join interrupted");
                Thread.currentThread().interrupt();
            }
            try {
                thUnicastUDPProcessor.join();
            } catch (InterruptedException e) {
                LOGGER.error("join interrupted");
                Thread.currentThread().interrupt();
            }
            try {
                thTCPPeerSocket.join();
            } catch (InterruptedException e) {
                LOGGER.error("join interrupted");
                Thread.currentThread().interrupt();
            }
            for (TCPProcessor processor : tcpProcessors.toArray(new TCPProcessor[0])) {
                processor.stop();
            }
            for (UDPProcessor processor : udpProcessors.toArray(new UDPProcessor[0])) {
                processor.stop();
            }
            for (PeerStateListener listener : getPeerStateListeners()) {
                listener.peerStopped(this);
            }
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    @Override
    public Request prepareRequest(String method, RequestOptions options, Object... params) {
        Request r = new Request();
        r.setMethod(method);
        r.addParams(params);
        r.setPeerFrom(this);
        r.setTimeout(options.getTimeout());
        r.setProcessId(createProcessId());
        return r;
    }

    @Override
    public Response unicastTCP(Peer remotePeer, Request request) throws PeerException {
        // int retry = 0;
        // PeerException ex = null;
        // while (retry++ < 3) {
        // try {
        return doUnicastTCP(remotePeer, request);
        // } catch (PeerException e) {
        // ex = e;
        // }
        // }
        // throw ex;
    }

    private Response doUnicastTCP(Peer remotePeer, Request request) throws PeerException {
        Socket clientSocket = null;
        ObjectOutputStream outToPeer = null;
        ObjectInputStream inFromPeer = null;
        try {
            if (isStarted()) {
                request.setRequestType(RequestType.UNICAST_TCP);
                //LOGGER.trace("Sending " + request.getRequestType() + " " + request);
                clientSocket = new Socket(remotePeer.getHost(), remotePeer.getUnicastTCPPort());
                clientSocket.setSoTimeout(request.getTimeout());
                outToPeer = new ObjectOutputStream(clientSocket.getOutputStream());
                inFromPeer = new ObjectInputStream(clientSocket.getInputStream());
                outToPeer.writeObject(request);
                Response response = (Response) inFromPeer.readObject();
                //LOGGER.trace("Received " + request.getRequestType() + " " + response);
                if (response.getReturnValue() instanceof NoOperationBaseException) {
                    throw new PeerException((Exception) response.getReturnValue());
                }
                return response;
            } else {
                throw new PeerNotStartedException();
            }
        } catch (SocketTimeoutException e) {
            throw new PeerException("cant execute request " + request + " on peer " + remotePeer + " " + e.toString());
        } catch (ClassNotFoundException | IOException e) {
            throw new PeerException("cant execute request " + request + " on peer " + remotePeer + " " + e.toString());
        } finally {
            try {
                if (inFromPeer != null) {
                    inFromPeer.close();
                }
                if (outToPeer != null) {
                    outToPeer.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) { //NOPMD
                //LOGGER.trace("socket couldn't be closed");
            }
        }
    }

    @Override
    public Response unicastUDP(Peer remotePeer, Request request) throws PeerException {
        try {
            if (isStarted()) {
                request.setRequestType(RequestType.UNICAST_UDP);
                LOGGER.debug("Sending " + request.getRequestType() + " " + request);
                byte[] bytes = request.getBytes();
                if (bytes.length > MAX_UDPPACKET_SIZE) {
                    //throw new PacketSizeExceededException();
                }
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remotePeer.getHost(),
                        remotePeer.getUnicastUDPPort());
                Object lock = new Object();
                unicastLocks.put(request.getId(), lock);
                unicastSocket.send(packet);
                LOGGER.debug("Will wait. Peer:" + StringUtils.shortId(getId()) + "Thread:"
                        + Thread.currentThread().getName());
                synchronized (lock) {
                    try {
                        lock.wait(request.getTimeout());
                        LOGGER.debug("Wait finished. Peer:" + StringUtils.shortId(getId()) + "Thread:"
                                + Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                unicastLocks.remove(request.getId());
                Response resp = htUnicastResponse.remove(request.getId());
                if (resp == null) {
                    throw new CallTimeoutException("PeerCallTimeoutException. Request:" + request.toString());
                }
                if (resp.getReturnValue() instanceof Exception) {
                    throw new PeerException((Exception) resp.getReturnValue());
                }
                return resp;
            } else {
                throw new PeerNotStartedException();
            }
        } catch (CallTimeoutException e) {
            throw e;
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    @Override
    public List<Response> blockingMulticast(Request request) throws PeerException {
        return blockingMulticast(request, -1, false, false);
    }

    public List<Response> blockingMulticast(Request request, int maxResponses, boolean throwExceptionIfFewerResponses,
            boolean failForPartialFailures) throws PeerException {
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
                LOGGER.debug("Will wait. Peer:" + StringUtils.shortId(getId()) + "Thread:"
                        + Thread.currentThread().getName());
                synchronized (lock) {
                    try {
                        lock.wait(request.getTimeout());
                        LOGGER.debug("Wait finished. Peer:" + StringUtils.shortId(getId()) + "Thread:"
                                + Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                blockingMulticastLocks.remove(request.getId());
                List<Response> resp = htBlockingMulticastResponse.remove(request.getId());
                if (resp.size() < maxResponses && throwExceptionIfFewerResponses) {
                    throw new CallTimeoutException("PeerCallTimeoutException. Request:" + request.toString());
                }
                if (failForPartialFailures) {
                    Iterator<Response> iter = resp.iterator();
                    while (iter.hasNext()) {
                        Response res = iter.next();
                        if (res.getReturnValue() instanceof Exception) {
                            throw new PeerException((Exception) res.getReturnValue());
                        }
                    }
                }
                return resp;
            } else {
                throw new PeerNotStartedException();
            }
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    @Override
    public void multicast(Request request) throws PeerException {
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
                throw new PeerNotStartedException();
            }
        } catch (IOException e) {
            throw new PeerException(e);
        }
    }

    private void getTcpPeerSocket() throws IOException {
        int i = 0;
        while (i < 1000) {
            try {
                UNICAST_TCP_PORT = UNICAST_TCP_PORT + i;
                tcpServerSocket = new ServerSocket(UNICAST_TCP_PORT);
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

    @Override
    public void start() throws PeerException {
        start(0);
    }

    @Override
    public void stop() throws PeerException {
        stop(0);
    }
}
