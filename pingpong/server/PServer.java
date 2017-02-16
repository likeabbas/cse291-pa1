package pingpong.server;

import pingpong.PingServer;
import pingpong.PingServerFactory;
import java.net.InetSocketAddress;
import rmi.Skeleton;
import rmi.Stub;
import rmi.RMIException;

public class PServer implements PingServer, PingServerFactory {
	private Skeleton<PingServerFactory> s;
	private Skeleton<PingServer> ps;

	public PServer() throws RMIException{
	    s = new Skeleton<PingServerFactory>(PingServerFactory.class, this);
		s.start();
		System.out.println("Starting PingServerFactory at address with");
		ps = null;
	}
 
	public static void main(String args[]) {
		try {
			PServer server = new PServer();
			InetSocketAddress a = server.getFactoryAddress();
			System.out.println("Starting PingServerFactory at address " 
				+ "(hostname, port) = (" +a.getHostName() + ", "
				+ a.getPort() + ")");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized String ping(int idNumber) {
		return new String("Pong " + idNumber);
	}


	public PingServer makePingServer() throws RMIException {
		if(ps == null) {
			ps = new Skeleton<PingServer>(PingServer.class, this);
			ps.start();
			InetSocketAddress a = ps.getAddress();
			System.out.println("Starting PingServer at address " 
			+ "(hostname, port) = (" +a.getHostName() + ", "
			+ a.getPort() + ")");
		}
		PingServer stub = null;
		try {
			stub = Stub.create(PingServer.class, ps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stub;
	}
	
	private InetSocketAddress getFactoryAddress() {
		return s.getAddress();
	}
}