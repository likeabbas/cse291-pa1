package rmi;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

class ServiceThread<T> implements Runnable {
	private Socket socket;
	private Skeleton<T> skeleton;

	public ServiceThread(Socket socket, Skeleton<T> skeleton) {
		this.socket   = socket;
		this.skeleton = skeleton;
	}
	
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
            for (int i = 0; i < argsLength; i++) {
                args[i] = istream.readObject();
            }

		} catch(Exception e) {
			skeleton.service_error(new RMIException("Error with request", e));
		}



	}
}