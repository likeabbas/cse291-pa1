package pingpong.client;

import pingpong.PingServer;
import pingpong.PingServerFactory;
import java.net.InetSocketAddress;
import rmi.Skeleton;
import rmi.Stub;

public class PingClient {
	public static void main (String args[]) {
		InetSocketAddress address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		try {
			PingServerFactory f = Stub.create(PingServerFactory.class,
										      address);

			PingServer ps = f.makePingServer();
		} catch (Exception e) {
			e.printStackTrace();
		}




	}
}