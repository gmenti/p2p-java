
import java.rmi.*;

public interface SuperNodeInterface extends Remote {
  public String[] listResources() throws RemoteException;
  public String[] listHashes() throws RemoteException;
  public String getLocation(String hash) throws RemoteException; // location = 127.0.0.1:2001
  public void register(String ip, int port, String[] resources, String[] hashs) throws RemoteException;
  public void status(String ip, int port) throws RemoteException;
}
