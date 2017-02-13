package rmi;

import java.net.*;
import java.lang.IllegalStateException;
import java.util.Arrays;
import java.lang.reflect.*;
import java.util.List;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.NoSuchMethodException;
import java.lang.ClassCastException;






/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub
{

    /** Creates a stub, given a skeleton with an assigned adress.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
        throws UnknownHostException
    {

        System.err.println("In T create(Class<T> c, Skeleton<T> skeleton)");

        if(c == null || skeleton == null) {
            System.err.println("Throw Null Pointer");
            throw new NullPointerException();
        }
        
        if(!skeleton.isRemoteInterface(c)) {
            System.err.println("!isRemoteInterface");
            throw new Error("All methods for class c must throw RMIException");
        }

        //TODO maybe separate running
        if(skeleton.getAddress() == null) {
            System.err.println("seperate address");
            throw new IllegalStateException();
        }

        System.err.println("stub.java - before calling handler");
        // check for no address found for local host
        InvocationHandler handler = new RMIInvocationHandler<T>(skeleton.getAddress(), c);
        try {
            T t = (T) java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(),
                                              new Class[] { c, ProxyDetails.class },
                                              handler);
            return t;
        } catch(ClassCastException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {

        System.err.println("In T create(Class<T> c, Skeleton<T> skeleton, String hostname)");

        if(c == null || skeleton == null || hostname == null) {
            throw new NullPointerException();
        }
        if(!skeleton.isRemoteInterface(c)) {
            throw new Error("All methods for class c must throw RMIException");
        }
        if(skeleton.getServerSocket().getLocalPort() == -1) {
            throw new IllegalStateException();
        }

        // check for no address found for local host
        InvocationHandler handler = new RMIInvocationHandler<T>(skeleton.getAddress(), c);
        try {
            T t = (T) java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(),
                                              new Class[] { c, ProxyDetails.class },
                                              handler);
            return t;
        } catch(ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
        System.err.println("In T create(Class<T> c, InetSocketAddress address)");

        if(c == null || address == null) {
            throw new NullPointerException();
        }

        Method[] methods = c.getMethods();
        for(Method method : methods) {
            List<Class<?>> exs = Arrays.asList(method.getExceptionTypes());
            if(!exs.contains(RMIException.class)) {
                throw new Error("All methods for class c must throw RMIException");
            }
        }


        // check for no address found for local host
        InvocationHandler handler = new RMIInvocationHandler<T>(address, c);
        try {
            T t = (T) java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(),
                                              new Class[] { c, ProxyDetails.class },
                                              handler);
            return t;
        } catch(ClassCastException e) {
            e.printStackTrace();
        }
        return null;


    }

}

interface ProxyDetails<T> {
    public Class<T> getCls(); 
    public InetSocketAddress getServerAddress();
}

class RMIInvocationHandler<T> implements InvocationHandler, ProxyDetails {

    private InetSocketAddress address;
    private Class<T> c;
    private static Method[] localMethods;

    static {
        try {
            localMethods = new Method[] {
                    Object.class.getMethod("equals", (Class []) new Class [] {Object.class}),
                    Object.class.getMethod("hashCode"), Object.class.getMethod("toString"),
                    ProxyDetails.class.getMethod("getServerAddress"),
                    ProxyDetails.class.getMethod("getCls")
            };  
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    
    public RMIInvocationHandler(InetSocketAddress address, Class<T> c) {
        this.c = c;
        this.address = address;
    }

    public InetSocketAddress getServerAddress() {
        return address;
    }

    public Class<T> getCls() {
        return c;
    }

    private boolean isLocalMethod(Method method) {
        for(Method m : localMethods) {
            if(method.equals(m)) {
                return true;
            }
        }
        return false;
    }
    private Object handleLocalMethods(Object proxy, Method method, Object[] args) {
        System.err.println("Inside handleLocalMethods");
        Object result;
        try {
            Method m = Object.class.getMethod("equals", (Class []) new Class [] {Object.class});

            T p = (T) proxy;
            if(method.equals(m)) {
                System.err.println("---inside equals case");
                if(args[0] != null) {
                    System.err.println("this.getCls() = " + this.getCls());
              
                    if(args[0] instanceof ProxyDetails) {
                        System.err.println("args[0].getCls() " + ((ProxyDetails)args[0]).getCls());
                    }
                }
                if(args[0] == null) {
                    result = false;
                } else if(!(args[0] instanceof ProxyDetails)) {
                    result = false;
                } else{
                    T arg = (T) args[0];
                    result = p.hashCode() == arg.hashCode();
                }
                return result;
            }
            
            m = Object.class.getMethod("hashCode");
            if(method.equals(m)) {
                return p.toString().hashCode();
            }

            m = Object.class.getMethod("toString");
            if(method.equals(m)) {
                Class<T> c = this.getCls();
                InetSocketAddress a = this.getServerAddress();
                StringBuilder sb = new StringBuilder("Remote interface: ");

                if(c != null) {
                    sb.append(c.getName());
                } else {
                    sb.append("null, (hostname, port): ");
                }

                if(a != null) {
                    sb.append("(" + a.getHostName() + ", " + a.getPort() + ")" );
                } else {
                    sb.append("(null, null)");
                }
                        
                return sb.toString();
            }

            m = ProxyDetails.class.getMethod("getServerAddress");
            if(method.equals(m)) {
                return this.getServerAddress();
            }

            m = ProxyDetails.class.getMethod("getCls");
            if(method.equals(m)) {
                return this.getCls();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws RMIException, Exception{
        System.err.println("In OBject invoke ");

        Object result = null;

        if(isLocalMethod(method)) {
            result = handleLocalMethods(proxy, method, args);
            return result;
        } else {
            
            Socket socket = new Socket();

                /*if(!Arrays.asList(this.getCls().getMethods()).contains(method)) {
                    throw new RMIException("Method " + method.getName() 
                        + " is not part of the remote interface " 
                        + this.getCls().getName());
                }*/

                // TODO check parameter types
            Class<?>[] paramTypes = method.getParameterTypes();
            ObjectOutputStream ostream; 
            ObjectInputStream  istream;
                
            try {

                socket.connect(address);

                ostream = new ObjectOutputStream(socket.getOutputStream());
                ostream.flush();
                istream = new ObjectInputStream(socket.getInputStream());

                for(Object arg : args) {
                    if (!(arg instanceof Serializable)) {
                        throw new RMIException("All objects passed to method must be serializable");
                    }
                }

                System.err.println("writing method name");
                ostream.writeObject(method.getName());
                ostream.writeObject(method.getParameterTypes());
                System.err.println("writing arg length");
                ostream.writeInt(args.length);


                System.err.println("writing args");
                for(Object arg : args) {
                    ostream.writeObject(arg);
                }

                System.err.println("closing ostream");

                System.err.println("reading result");
                result = istream.readObject();
                System.err.println("finished reading result");
            } catch (Exception e) {
                throw new RMIException("Error invoking method: " + method.getName(), e);
            }


            System.err.println("Remote call returned: " + result);
            if(result instanceof Exception) {
                throw (Exception)result;
            }

            try {

                istream.close();
                ostream.close();
                return result;
    

            } catch (Exception e) {
                throw new RMIException("Error invoking method: " + method.getName(), e);
            }

        }
    }
}

