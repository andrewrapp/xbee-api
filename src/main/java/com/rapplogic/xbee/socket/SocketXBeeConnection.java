package com.rapplogic.xbee.socket;

import com.rapplogic.xbee.AbstractXBeeConnection;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

// TODO handle reconnects
public class SocketXBeeConnection extends AbstractXBeeConnection {

	private final static Logger log = Logger.getLogger(SocketXBeeConnection.class);

	private Socket socket;
	private XBeeSocketOutputStream xBeeSocketOutputStream;
	private OutputStream out;

	public SocketXBeeConnection(final String host, final Integer port) throws ServerNotAvailableException {
		init(host, port);
	}

	@Override
	public OutputStream getOutputStream() {
		return xBeeSocketOutputStream;
	}

	private ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(false);
			return thread;
		}
	});

	private void init(final String host, final Integer port) throws ServerNotAvailableException {
		Socket socket;

		try {
			socket = new Socket(host, port);
			log.info("Successfully connected to socket server");
		} catch (IOException e) {
			log.warn("Unable to connect to host:port " + host + ":" + port);
			throw new ServerNotAvailableException("Unable to connect to host:port " + host + ":" + port, e);
		}

		try {
			out = socket.getOutputStream();
			xBeeSocketOutputStream = new XBeeSocketOutputStream();
		} catch (IOException e) {
			throw new RuntimeException("Outputstream not available for socket", e);
		}

		try {
			socket.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("Inputstream not available for socket", e);
		}

		this.socket = socket;
		this.pipeSocketInputStreamToXBee();
	}

	private void pipeSocketInputStreamToXBee() {

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				int b = 0;
				
				try {
					while ((b = socket.getInputStream().read()) != -1) {
						pipeToInputStream(b);
					}		
					
					log.debug("End of socket input stream.. exiting");
				} catch (IOException e) {
					log.warn("Error reading from socket input stream " + e.toString() + "... closing socket");
					tryClose();
				} catch (Throwable t) {
					log.error("Error reading from input  to the xbee output stream", t);

					try {
						close();
					} catch (Throwable t2) {}

				}
			}
		});
	}

	public class XBeeSocketOutputStream extends OutputStream {

		@Override
		public void write(int i) throws IOException {
			try {
				out.write(i);
			} catch (IOException e) {
				log.warn("Failed to write byte " + i + " to output stream. closing socket. error: " + e.toString() + ", socket " + socketStatus());
				tryClose();
				throw e;
			}
		}
	}

	private String socketStatus() {
		return "isConnected: " + socket.isConnected() + ", isBound: " + socket.isBound() + ", isClosed: " + socket.isClosed();
	}

	private void tryClose() {
		try {
			log.info("Closing socket");
			close();
		} catch (Exception e) {
			log.warn("Failed to close socket " + e.toString());
		}
	}

	@Override
	public void close() throws IOException {
		socket.close();
		executorService.shutdownNow();
	}	
}
