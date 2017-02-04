package rmi;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

class ServiceThread<T> implements Runnable {
	private Socket socket;
	private Skeleton<T> skeleton;

	public ServiceThread(Socket socket, Skeleton<T> skeleton) {
		this.socket = socket;
		this.skeleton = skeleton;
	}
	
	public void run() {
		// get in/out streams
		try {
			ObjectOutputStream ostream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream istream = new ObjectInputStream(socket.getInputStream());

		} catch(Exception e) {
			skeleton.service_error(new RMIException("Error with request", e));
		}



	}
}