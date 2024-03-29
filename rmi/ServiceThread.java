package rmi;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.*;

class ServiceThread<T> implements Runnable {
	private Socket socket;
	private Skeleton<T> skeleton;

	public ServiceThread(Socket socket, Skeleton<T> skeleton) {
		this.socket   = socket;
		this.skeleton = skeleton;
	}
	
    @Override
	public void run() {
		// get in/out streams
        //synchronized(skeleton) {
            try {
                ObjectOutputStream ostream = new ObjectOutputStream(socket.getOutputStream());
                ostream.flush();
                ObjectInputStream istream  = new ObjectInputStream(socket.getInputStream());

                String methodName = (String) istream.readObject();
                System.err.println("methodName: " + methodName);
                
                Class<?>[] paramTypes = (Class<?>[]) istream.readObject();
                System.err.println("paramTypes.length: " + paramTypes.length);

                Object[] args = new Object[paramTypes.length];
                if(paramTypes.length > 0) {
                    for (int i = 0; i < args.length; i++) {
                        args[i] = istream.readObject();
                        System.err.println("args["+i+"] = " + args[i]);
                    }
                }
                
                // TODO catch specific exception
                Object result = null;
                Method method = null;
                boolean exceptionThrown = false;
                System.err.println("getting method");
                try {
                    method = skeleton.getCls().getMethod(methodName, paramTypes);
                } catch (NoSuchMethodException e) {
                    System.err.println("Inside NoSuchMethodException for getMethod in ServiceThread");
                    exceptionThrown = true;
                    result = new RMIException("No Such Method");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.err.println("after getting method");
                T ror = null;
                if (!exceptionThrown)
                    ror = skeleton.getRemoteObject();


                try {
                    if (!exceptionThrown) {
                        System.err.println("about to invoke " + method.getName());
                        System.err.println("really - about to invoke");
                        result = method.invoke(ror, args);
                    }
                    System.err.println("after invoke");
                } catch (InvocationTargetException e) {
                    System.err.println("inside catch for invoke");
                    exceptionThrown = true;
                    result = e.getCause();
                } catch(Exception e) {
                    e.printStackTrace();
                }

                System.err.println("writing exception: " + exceptionThrown);
                ostream.writeBoolean(exceptionThrown);
                System.err.println("writing result: " + result);
                ostream.writeObject(result);
                istream.close();
                ostream.close();
                socket.close();

            } catch(Exception e) {
                System.err.println("caught exception in service thread: " + e);
                skeleton.service_error(new RMIException("Error with request", e));
            }

 //        }

	}
}
