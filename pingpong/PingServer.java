package pingpong;
import rmi.RMIException;
public interface PingServer {
	public String ping(int idNumber) throws RMIException;
}