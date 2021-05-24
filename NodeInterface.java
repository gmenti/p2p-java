
import java.rmi.*;

public interface NodeInterface extends Remote {
  public String getContent(String hash) throws RemoteException;
}
