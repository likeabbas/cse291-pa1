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

	public PServer(int port) throws RMIException{
        InetSocketAddress a = new InetSocketAddress(port);
	    s = new Skeleton<PingServerFactory>(PingServerFactory.class, this, a);
		s.start();
		System.out.println("Starting PingServerFactory at address with");
		ps = null;
	}
 
	public static void main(String args[]) {
		try {
			PServer server = new PServer(Integer.parseInt(args[0]));
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
      InetSocketAddress a = new InetSocketAddress(9876);
			ps = new Skeleton<PingServer>(PingServer.class, this, a);
			ps.start();
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
