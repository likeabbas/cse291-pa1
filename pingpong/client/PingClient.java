package pingpong.client;

import pingpong.PingServer;
import pingpong.PingServerFactory;
import java.net.InetSocketAddress;
import rmi.Skeleton;
import rmi.Stub;


public class PingClient {
    private static final int NUM_TESTS = 4;
	public static void main (String args[]) {
		InetSocketAddress address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        PingServer ps = null;
        int count = 0;
		try {
			PingServerFactory f = Stub.create(PingServerFactory.class,
										      address);

			ps = f.makePingServer();
            if(ps != null) {
                for(int i = 0; i < NUM_TESTS; ++i) {
                    String str = ps.ping(i);
                        if(str.equals("Pong " + i)) {
                            count++;
                        }
                }
            } else {
                System.err.println("\n\t\tFailed to retreive stub for PingServer...");
            }
		} catch (Exception e) {
            System.err.println("Exception occurred");
		} finally {
            System.out.println(count + " Tests Completed, " 
                + (NUM_TESTS - count) + " Tests Failed.");

        }
                




	}
}
