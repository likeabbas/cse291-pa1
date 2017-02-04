package rmi;

import java.net.*;

class SocketListener<T> implements Runnable {
	private ServerSocket servSock;
	private Skeleton<T> skeleton;
    private volatile boolean finished;

	public SocketListener(ServerSocket servSock, Skeleton<T> skeleton) {
		this.servSock = servSock;
		this.skeleton = skeleton;
        this.finished = false;
	}

	public void stopMe() {
        this.finished = true;
    }

	@Override
	public void run() { // throws IOException
		while(!this.finished) {

			try {
				System.err.println("before accept call");
				Socket socket = servSock.accept();
				System.err.println("after accept call");
				new Thread(skeleton.getServiceGroup(), new ServiceThread<T>(socket, skeleton)).start();

			} catch(SocketException e) {
				System.out.println("socket exception");
			} catch(Exception e) {
				skeleton.listen_error(e);
			}

		}
	}
}