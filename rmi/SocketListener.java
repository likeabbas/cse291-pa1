package rmi;

import java.net.*;

class SocketListener<T> implements Runnable {
	private ServerSocket servSock;
	private Skeleton<T> skeleton;

	public SocketListener(ServerSocket servSock, Skeleton<T> skeleton) {
		this.servSock = servSock;
		this.skeleton = skeleton;
	} 

	@Override
	public void run() { // throws IOException
		while(true) {

			try {

				Socket socket = servSock.accept();

				new Thread(new ServiceThread<T>(socket, skeleton)).start();

			} catch(Exception e) {
				skeleton.listen_error(e);
			}

		}
	}
}