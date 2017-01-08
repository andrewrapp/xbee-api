package com.rapplogic.xbee.socket;

import com.github.rholder.retry.*;
import com.rapplogic.xbee.AbstractXBeeConnection;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;

// TODO move to client pom or to xbee-api

public class SocketXBeeConnection extends AbstractXBeeConnection {

	private final static Logger log = Logger.getLogger(SocketXBeeConnection.class);
	
	private Socket socket;
	private OutputStream out;
	
	Retryer<Socket> retryer = RetryerBuilder.<Socket>newBuilder()
	        .retryIfExceptionOfType(IOException.class)
	        .withWaitStrategy(WaitStrategies.fibonacciWait(30000, TimeUnit.SECONDS))
	        .withStopStrategy(StopStrategies.stopAfterDelay(1000*60*60)) // .withStopStrategy(StopStrategies.stopAfterDelay(1000*60*60))
	        .build();
	
	public SocketXBeeConnection(final String host, final Integer port) throws ServerNotAvailableException {
		Socket socket;
		
		try {
		    socket = retryer.call(new Callable<Socket>() {
				@Override
				public Socket call() throws Exception {
					try {
						return new Socket(host, port);			
					} catch (IOException e) {
						log.warn("Unable to connect to host:port " + host + ":" + port);
						throw e;
					}
				}
			});
		    
		    log.info("Successfully connected to socket server");
		} catch (RetryException e) {
			throw new ServerNotAvailableException("Max retries reached", e);
		} catch (ExecutionException e) {
		    throw new RuntimeException("Unexpected error in retryer", e);
		}
		
		try {
			out = socket.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException("Outstream not available for socket", e);
		}
		
		try {
			socket.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("Inputstream not available for socket", e);
		}
		
		this.socket = socket;		
		this.pipeSocketInputStreamToXBee();
	}	
	
	// TODO handle socket failures/reconnect
//	Caused by: java.net.SocketException: Broken pipe
//	at java.net.SocketOutputStream.socketWrite0(Native Method)
//	at java.net.SocketOutputStream.socketWrite(SocketOutputStream.java:92)
//	at java.net.SocketOutputStream.write(SocketOutputStream.java:115)
//	at com.rapplogic.xbee.api.XBee.sendPacket(XBee.java:252)
//	at com.rapplogic.xbee.api.XBee.sendPacket(XBee.java:225)
//	at com.rapplogic.xbee.api.XBee.sendRequest(XBee.java:210)

	private ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
		   @Override
		   public Thread newThread(Runnable runnable) {
		      Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		      thread.setDaemon(false);
		      return thread;
		   }
	});
	
	private void pipeSocketInputStreamToXBee() {

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				int b = 0;
				
				try {
					while ((b = socket.getInputStream().read()) != -1) {
						write(b);
					}		
					
					log.debug("End of socket input stream.. exiting");
				} catch (IOException e) {
					if (e instanceof SocketException && "Socket closed".equals(e.getMessage())) {
						// normal close
						log.info("Socket closed");
					} else {
						log.warn("Error reading from socket input stream " + e.toString());						
					}
				} catch (Throwable t) {
					log.error("Unable to pipe socket input to the xbee output stream", t);
				}
			}
		});
	}
	
	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public void close() throws IOException {
		socket.close();
		executorService.shutdownNow();
	}	
}
