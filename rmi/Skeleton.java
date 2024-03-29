package rmi;

import java.net.*;
import java.lang.NullPointerException;
import java.lang.Error;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.*;
import java.lang.InterruptedException;
import java.io.IOException;
import java.util.Vector;



/** RMI skeleton

    <p>
    A skeleton encapsulates a multithreaded TCP server. The server's clients are
    intended to be RMI stubs created using the <code>Stub</code> class.

    <p>
    The skeleton class is parametrized by a type variable. This type variable
    should be instantiated with an interface. The skeleton will accept from the
    stub requests for calls to the methods of this interface. It will then
    forward those requests to an object. The object is specified when the
    skeleton is constructed, and must implement the remote interface. Each
    method in the interface should be marked as throwing
    <code>RMIException</code>, in addition to any other exceptions that the user
    desires.

    <p>
    Exceptions may occur at the top level in the listening and service threads.
    The skeleton's response to these exceptions can be customized by deriving
    a class from <code>Skeleton</code> and overriding <code>listen_error</code>
    or <code>service_error</code>.
*/
public class Skeleton<T>
{
    private Class<T> c;
    private T server;
    private InetSocketAddress address;
    private SocketListener<T> listener;
    private Thread listenerThread;
    private boolean isRunning = false;
    private ThreadGroup serviceGroup;
    private Vector<Thread> serviceThreads;
    private ServerSocket servSocket;

    private static final int MAX_Q_CONNECTIONS = 10;

    /** Creates a <code>Skeleton</code> with no initial server address. The
        address will be determined by the system when <code>start</code> is
        called. Equivalent to using <code>Skeleton(null)</code>.

        <p>
        This constructor is for skeletons that will not be used for
        bootstrapping RMI - those that therefore do not require a well-known
        port.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server) throws NullPointerException, Error
    {
        if(server == null || c == null) {
            throw new NullPointerException("Server and class specified to Skeleton must not be null");
        } 

        if(!isRemoteInterface(c)) {
            throw new Error("All methods for server class must throw RMIException");
        }

        this.c = c; this.server = server; this.address = null;
    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

        <p>
        This constructor should be used when the port number is significant.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @param address The address at which the skeleton is to run. If
                       <code>null</code>, the address will be chosen by the
                       system when <code>start</code> is called.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
        throws NullPointerException, Error
    {
        this(c, server);
        this.address = address;

    }

    public boolean isRemoteInterface(Class<T> c) {
        Method[] methods = c.getMethods();
        for(Method method : methods) {
            List<Class<?>> exs = Arrays.asList(method.getExceptionTypes());
            if(!exs.contains(RMIException.class)) {
                return false;
            }
        }
        return true;
    }
    /** Called when the listening thread exits.

        <p>
        The listening thread may exit due to a top-level exception, or due to a
        call to <code>stop</code>.

        <p>
        When this method is called, the calling thread owns the lock on the
        <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
        calling <code>start</code> or <code>stop</code> from different threads
        during this call.

        <p>
        The default implementation does nothing.

        @param cause The exception that stopped the skeleton, or
                     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
        System.err.println("WHYYY ARE WE CALLING DEFAULT STOPPED!!!");
        if(cause == null) {
            isRunning = false;
        }
        isRunning = false;
    }

    /** Called when an exception occurs at the top level in the listening
        thread.

        <p>
        The intent of this method is to allow the user to report exceptions in
        the listening thread to another thread, by a mechanism of the user's
        choosing. The user may also ignore the exceptions. The default
        implementation simply stops the server. The user should not use this
        method to stop the skeleton. The exception will again be provided as the
        argument to <code>stopped</code>, which will be called later.

        @param exception The exception that occurred.
        @return <code>true</code> if the server is to resume accepting
                connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        stopped(null);
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

        <p>
        The default implementation does nothing.

        @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
        System.err.println("CALLING DEFAULT SERVICE_ERROR");
    }

    /** Starts the skeleton server.

        <p>
        A thread is created to listen for connection requests, and the method
        returns immediately. Additional threads are created when connections are
        accepted. The network address used for the server is determined by which
        constructor was used to create the <code>Skeleton</code> object.

        @throws RMIException When the listening socket cannot be created or
                             bound, when the listening thread cannot be created,
                             or when the server has already been started and has
                             not since stopped.
     */
    public synchronized void start() throws RMIException
    {
        System.err.println("beginning of start");
        try {
            // make sure we have an address
            if(address == null) {
                System.err.println("address is null");
                servSocket = new ServerSocket(0);
                address = (InetSocketAddress)servSocket.getLocalSocketAddress();
            } else {
                servSocket = new ServerSocket();
                servSocket.bind(address);
            }
            
            listener = new SocketListener<T>(servSocket, this);
            listenerThread = new Thread(listener);
            serviceThreads = new Vector<Thread>();
            listenerThread.start();
            this.isRunning = true;

        } catch (IOException e) {
            throw new RMIException("failed to start Skeleton server", e);
        }

    }

    /** Stops the skeleton server, if it is already running.

        <p>
        The listening thread terminates. Threads created to service connections
        may continue running until their invocations of the <code>service</code>
        method return. The server stops at some later time; the method
        <code>stopped</code> is called at that point. The server may then be
        restarted.
     */
    public synchronized void stop()
    {
        System.err.println("inside stop");

        try {
            System.err.println("check running");
            if (isRunning) {
                isRunning = false;
                System.err.println("about to close socket");

                if(listener != null) {
                    listener.stopMe();
                    servSocket.close();
                    listenerThread.join();
                    stopped(null);
                }

                if(serviceThreads != null) {
                    for(Thread thread : serviceThreads) {
                        thread.join();
                    }
                    serviceThreads.clear();
                }
                System.err.println("after stopped");

            }
        } catch (InterruptedException e) {
            System.err.println("InterruptedException");
            e.printStackTrace();
            stopped(e);

        } catch(IOException e) {
            System.err.println("IOException");
            e.printStackTrace();
            stopped(e);
        }

        //this.stopped(null);
        System.err.println("end of stop");
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    public Vector<Thread> getServiceThreads() {
        return serviceThreads;
    }

    public ServerSocket getServerSocket() {
        return servSocket;
    }

    public T getRemoteObject() {
        return server;
    }
    public Class<T> getCls() {
        return c;
    }

}
