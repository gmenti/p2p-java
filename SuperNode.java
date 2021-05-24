import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class SuperNode extends UnicastRemoteObject implements SuperNodeInterface {
  public ArrayList<String> resourcesHashes = new ArrayList<String>();
  public ArrayList<String> resourcesName = new ArrayList<String>();
  public ArrayList<String> resourcesHostname = new ArrayList<String>();
  public ArrayList<Integer> resourcesPort = new ArrayList<Integer>();
  public Map<String, Date> lastCheckedTimeMap = new HashMap<String, Date>();
  public boolean propagated = true;

  public SuperNode() throws RemoteException {

  }

  public void propagate(boolean value) {
    propagated = !value;
  }

  private boolean existsResource(String hash) {
    return indexOfResource(hash) >= 0;
  }

  public int indexOfResource(String hash) {
    for (int i = 0; i < resourcesHashes.size(); i++) {
      if (resourcesHashes.get(i).equals(hash)) {
        return i;
      }
    }
    return -1;
  }

  public boolean addResource(String hostname, int port, String resourceName, String hash) {
    if (!existsResource(hash)) {
      resourcesName.add(resourceName);
      resourcesHostname.add(hostname);
      resourcesPort.add(port);
      resourcesHashes.add(hash);
      System.out.println("Added resource " + resourceName + ", " + hash + " from " + hostname + ":" + port);
      propagate(true);
      return true;
    }
    return false;
  }

  public String[] listResources() throws RemoteException {
    String[] values = new String[resourcesName.size()];
    for (int i = 0; i < resourcesName.size(); i++) {
      values[i] = resourcesName.get(i);
    }
    return values;
  }

  public String[] listHashes() throws RemoteException {
    String[] values = new String[resourcesHashes.size()];
    for (int i = 0; i < resourcesHashes.size(); i++) {
      values[i] = resourcesHashes.get(i);
    }
    return values;
  }

  public String getLocation(String resourceName) throws RemoteException {
    int index = indexOfResource(resourceName);
    if (index >= 0) {
      return resourcesHostname.get(index) + ":" + Integer.toString(resourcesPort.get(index));
    }
    return null;
  }

  public void register(String clientIp, int clientPort, String[] resources, String[] hashes) throws RemoteException {
    for (int i = 0; i < resources.length; i++) {
      addResource(clientIp, clientPort, resources[i], hashes[i]);
    }
    status(clientIp, clientPort);
  }

  public void status(String clientIp, int clientPort) throws RemoteException {
    lastCheckedTimeMap.put(clientIp + ":" + clientPort, new Date());
  }
}
