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
		try {
			ObjectOutputStream ostream = new ObjectOutputStream(socket.getOutputStream());
            ostream.flush();
			ObjectInputStream istream  = new ObjectInputStream(socket.getInputStream());

            String methodName = (String) istream.readObject();
            System.err.println("methodName: " + methodName);
            
            int argsLength = (int) istream.readInt();
            System.err.println("argsLength: " + argsLength);

            Object[] args = new Object[argsLength];
            Class<?>[] argClasses = new Class<?>[argsLength];
            for (int i = 0; i < argsLength; i++) {
                args[i] = istream.readObject();
                if( args[i].getClass().isPrimitive()) {
                    argClasses[i] = (Class<?>)args[i].getClass().getField("TYPE").get(null);
                   // argClasses[i] = boolean.class;
                } else {
                    argClasses[i] = args[i].getClass();
                }
            }
            
            // TODO catch specific exception
            System.err.println("getting method");
            Method method = skeleton.getCls().getMethod(methodName, argClasses);
            System.err.println("after getting method");
            T ror = skeleton.getRemoteObject();

            Object result;
            try {
                result = method.invoke(ror, args);
            } catch (Exception e) {
                System.err.println("inside catch for invoke");
                result = e;
            }

            System.err.println("writing result: " + result);
            ostream.writeObject(result);
            istream.close();
            ostream.close();
            socket.close();

		} catch(Exception e) {
            System.err.println("caught exception in service thread: " + e);
			skeleton.service_error(new RMIException("Error with request", e));
		}

	}
}
