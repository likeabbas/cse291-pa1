package pingpong;
import pingpong.PingServer;
import rmi.RMIException;
public interface PingServerFactory {
	public PingServer makePingServer() throws RMIException;
}