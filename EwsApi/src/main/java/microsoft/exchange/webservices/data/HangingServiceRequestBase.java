package microsoft.exchange.webservices.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

/**
 * Enumeration of reasons that a hanging request may disconnect.
 */
enum HangingRequestDisconnectReason {

	/** The server cleanly closed the connection. */
	Clean,

	/** The client closed the connection. */
	UserInitiated,

	/** The connection timed out do to a lack of a heartbeat received. */
	Timeout,

	/** An exception occurred on the connection */
	Exception
}

/**
 * Represents a collection of arguments for the 
 * HangingServiceRequestBase.HangingRequestDisconnectHandler
 * delegate method.
 */
class HangingRequestDisconnectEventArgs { 

	/**
	 * Initializes a new instance of the 
	 * HangingRequestDisconnectEventArgs class.
	 * 
	 * @param reason The reason.
	 * @param exception The exception.
	 */
    protected HangingRequestDisconnectEventArgs(
        HangingRequestDisconnectReason reason,
        Exception exception) {
        this.reason = reason;
        this.exception = exception;
    }

    private HangingRequestDisconnectReason reason;

    /**
	 * Gets the reason that the user was disconnected.
	 * 
	 * @return reason The reason.
	 */
    public HangingRequestDisconnectReason getReason() {
    	return reason;
    }
    
    /**
	 * Sets the reason that the user was disconnected.
	 * 
	 * @param value The reason.
	 */
    protected void setReason(HangingRequestDisconnectReason value) {
    	reason = value;
    }

    private Exception exception;
    
    /**
	 * Gets the exception that caused the disconnection. Can be null.
	 * 
	 * @return exception The Exception.
	 */
    public Exception getException() {
    	return exception;
    }
    
    /**
	 * Sets the exception that caused the disconnection. Can be null.
	 * 
	 * @param value The Exception.
	 */
    protected void setException(Exception value) {
    	exception = value;
    }
}

/**
 * Represents an abstract, hanging service request.
 */
abstract class HangingServiceRequestBase extends ServiceRequestBase {

	protected interface IHandleResponseObject {

		/**
		 * Callback delegate to handle asynchronous responses.
		 * 
		 * @param response
		 *            Response received from the server
		 * @throws ArgumentException 
		 */
	    void handleResponseObject(Object response) throws ArgumentException;
	}
	
	private static final int BufferSize = 4096;

	/**
	 * Test switch to log all bytes that come across the wire.
	 * Helpful when parsing fails before certain bytes hit the trace logs.
	 */
	protected static boolean LogAllWireBytes = false;

	/**
	 * Callback delegate to handle response objects
	 */
	private IHandleResponseObject responseHandler;

	/**
	 * Response from the server.
	 */
	private HttpWebRequest response;

	/**
	 * Request to the server.
	 */
	private HttpWebRequest request;

	/**
	 * Xml reader used to parse the response.
	 */
	private EwsServiceMultiResponseXmlReader ewsXmlReader;

	/**
	 * Expected minimum frequency in responses, in milliseconds.
	 */
	protected int heartbeatFrequencyMilliseconds;

	protected interface IHangingRequestDisconnectHandler {

		/**
		 * Delegate method to handle a hanging request disconnection.
		 * 
		 * @param sender
		 *            The object invoking the delegate.
		 * @param args, Event data.
		 */
	     void hangingRequestDisconnectHandler(Object sender,
	    		 HangingRequestDisconnectEventArgs args);

	}
	
	/***
	 * Disconnect events Occur when the hanging request is disconnected.
	 */
	private List<IHangingRequestDisconnectHandler> onDisconnectList = 
		new ArrayList<IHangingRequestDisconnectHandler>();

	/***
	 * Set event to happen when property disconnect.
	 * 
	 * @param disconnect
	 *            disconnect event
	 */
	protected void addOnDisconnectEvent(
			IHangingRequestDisconnectHandler disconnect) {
		onDisconnectList.add(disconnect);
	}

	/***
	 * Remove the event from happening when property disconnect.
	 * 
	 * @param disconnect
	 *            disconnect event
	 */
	protected void removeDisconnectEvent(
			IHangingRequestDisconnectHandler disconnect) {
		onDisconnectList.remove(disconnect);
	}

	/**
	 * * Clears disconnect events list.
	 */
	protected void clearDisconnectEvents() {
		onDisconnectList.clear();
	}

	/***
	 * Initializes a new instance of the HangingServiceRequestBase class.
	 * 
	 * @param service
	 *            The service.
	 * @param handler
	 * 			  Callback delegate to handle response objects
	 * @param heartbeatFrequency
	 * 			  Frequency at which we expect heartbeats, in milliseconds.
	 */
	protected HangingServiceRequestBase(ExchangeService service,
			IHandleResponseObject handler, int heartbeatFrequency) 
	throws ServiceVersionException {
		super(service);
		this.responseHandler = handler;
		this.heartbeatFrequencyMilliseconds = heartbeatFrequency;
	}

	/***
	 * Exectures the request.
	 */
	protected void internalExecute() throws ServiceLocalException, Exception {
		synchronized (this) {
			OutParam<HttpWebRequest> outParam = new OutParam<HttpWebRequest>();
			this.response = this.validateAndEmitRequest(outParam);
			this.request = outParam.getParam();
			this.internalOnConnect();
		}
	}

	/***
	 * Parses the responses.
	 * @param state
	 * 			 The state.
	 */
	private void parseResponses(Object state) {
		UUID traceId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		HangingTraceStream tracingStream = null;

		try {
			InputStream responseStream = this.response.getInputStream();
			tracingStream = new HangingTraceStream(responseStream,
					this.getService());

			

			while (this.isConnected()) {
				this.ewsXmlReader = EwsServiceMultiResponseXmlReader.create(
					tracingStream, 
					this.getService());//b'coz of java excep, first time it will wrk, 2nd time 
				Object responseObject = this.readResponse(this.ewsXmlReader);

				this.responseHandler.handleResponseObject(responseObject);
			}
		}
		catch (SocketTimeoutException ex) {
			// The connection timed out.
			this.disconnect(HangingRequestDisconnectReason.Timeout, ex);
			return;
		}
		catch (UnknownServiceException ex) {
			// Stream is closed, so disconnect.
			this.disconnect(HangingRequestDisconnectReason.Exception, ex);
			return;
		}
		catch (ObjectStreamException ex) {
			// Stream is closed, so disconnect.
			this.disconnect(HangingRequestDisconnectReason.Exception, ex);
			return;
		}
		catch (IOException ex) {
			// Stream is closed, so disconnect.
			this.disconnect(HangingRequestDisconnectReason.Exception, ex);
			return;
		}
		catch (UnsupportedOperationException ex) {
			ex.printStackTrace();
			// This is thrown if we close the stream during a 
			//read operation due to a user method call.
			// Trying to delay closing until the read finishes
			//simply results in a long-running connection.
			this.disconnect(HangingRequestDisconnectReason.UserInitiated, null);
			return;
		}
		catch (Exception ex) {
			// Stream is closed, so disconnect.
			this.disconnect(HangingRequestDisconnectReason.Exception, ex);
			return;
		}
		finally {
			ByteArrayOutputStream responseCopy;

			if (tracingStream != null &&
					this.getService().isTraceEnabledFor(TraceFlags.EwsResponse) &&
					(responseCopy = tracingStream.GetResponseCopy()) != null) {
				this.getService().traceXml(
						TraceFlags.EwsResponse,
						responseCopy);
			}
		}
	}

	private boolean isConnected;

	/***
	 * Gets a value indicating whether this instance is connected.
	 * 
	 * @return true, if this instance is connected; otherwise, false
	 */
	protected boolean isConnected() {
		return this.isConnected;
	}

	private void setIsConnected(boolean value) {
		this.isConnected = value;
	}

	/***
	 * Disconnects the request.
	 */
	protected void disconnect() {
		synchronized (this) {
			this.request.close();
			this.response.close();
			this.disconnect(HangingRequestDisconnectReason.UserInitiated, null);
		}
	}

	/***
	 * Disconnects the request with the specified reason and exception.
	 * @param reason
	 * 			  The reason.
	 * @param exception
	 * 			  The exception.
	 */
	protected void disconnect(HangingRequestDisconnectReason reason,
			Exception exception) {
		if (this.isConnected()) {
			this.internalOnDisconnect(reason, exception);
		}
	}

	/***
	 * Perform any bookkeeping needed when we connect
	 */
	private void internalOnConnect() throws XMLStreamException, 
	IOException, EWSHttpException {
		if (!this.isConnected()) {
			this.isConnected = true;

			if (this.getService().isTraceEnabledFor(TraceFlags.EwsResponseHttpHeaders)) {
				// Trace Http headers
				this.getService().traceHttpResponseHeaders(
						TraceFlags.EwsResponseHttpHeaders,
						this.response);
			}
			int poolSize = 1;
			 
		    int maxPoolSize = 1;
		 
		    long keepAliveTime = 10;
		 
		    final ArrayBlockingQueue<Runnable> queue = 
		    	new ArrayBlockingQueue<Runnable>(
		            1);
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(poolSize,
					maxPoolSize,
	                keepAliveTime, TimeUnit.SECONDS, queue);
			threadPool.execute(new Runnable()
	        {
	            public void run() {
	            	parseResponses(null);
	            }
	        });
			threadPool.shutdown();
		}
	}

	/***
	 * Perform any bookkeeping needed when we disconnect (cleanly or forcefully)
	 * @param reason
	 * 			  The reason.
	 * @param exception
	 * 			  The exception.
	 */
	private void internalOnDisconnect(HangingRequestDisconnectReason reason, 
			Exception exception){
		if (this.isConnected()) {
			this.isConnected = false;
			for (IHangingRequestDisconnectHandler disconnect : onDisconnectList) {
				disconnect.hangingRequestDisconnectHandler(this,
						new HangingRequestDisconnectEventArgs(reason, exception));
			}
		}
	}

	/***
	 * Reads any preamble data not part of the core response.
	 * @param ewsXmlReader
	 * 			  The EwsServiceXmlReader.
	 * @throws Exception 
	 */
	@Override
	protected void readPreamble(EwsServiceXmlReader ewsXmlReader)
	throws Exception {
		// Do nothing.
		try {
			ewsXmlReader.read(new XMLNodeType(XMLNodeType.START_DOCUMENT));
		}
		catch (XmlException ex){
			throw new ServiceRequestException(Strings.
					ServiceResponseDoesNotContainXml, ex);
		}
		catch (ServiceXmlDeserializationException ex){
			throw new ServiceRequestException(Strings.
					ServiceResponseDoesNotContainXml, ex);
		}
	}
}
