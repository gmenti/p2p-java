import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Date;
import java.util.Iterator;

public class UDPMulticastServer extends UDPMulticast {
	public String hostname;
	public int port;
	public SuperNode superNode;

	public UDPMulticastServer(String[] args) {
		super(args[1].split(":"));

		hostname = args[2].split(":")[0];
		port = Integer.parseInt(args[2].split(":")[1]);

		initRMI();
	}

	public void initRMI() {
		try {
			System.setProperty("java.rmi.server.hostname", hostname);
			LocateRegistry.createRegistry(port);
			System.out.println("Java RMI registry created.");

			superNode = new SuperNode();
			String client = "rmi://" + hostname + ":" + port + "/supernode";
			Naming.rebind(client, superNode);
		} catch (RemoteException | MalformedURLException e) {
			System.out.println("Java RMI registry already exists.");
		}
	}

	public void sendResource(int i) throws IOException {
		System.out.println("Sent resource" + ":" +  superNode.resourcesHostname.get(i) + ":" + superNode.resourcesPort.get(i) + ":" + superNode.resourcesName.get(i) + ":" + superNode.resourcesHashes.get(i));
		sendMessage("resource" + ":" +  superNode.resourcesHostname.get(i) + ":" + superNode.resourcesPort.get(i) + ":" + superNode.resourcesName.get(i) + ":" + superNode.resourcesHashes.get(i));
	}

	public void sendRemovedResorce(int i) throws IOException {
		System.out.println("Removed resource" + ":" +  superNode.resourcesHostname.get(i) + ":" + superNode.resourcesPort.get(i) + ":" + superNode.resourcesName.get(i) + ":" + superNode.resourcesHashes.get(i));
		sendMessage("removed_resource" + ":" + superNode.resourcesHostname.get(i) + ":" + superNode.resourcesPort.get(i));
	}

	public void sendResources() throws IOException {
		for (int i = 0; i < superNode.resourcesName.size(); i++) {
			sendResource(i);
		}
	}

	public static void execute(String[] args) throws IOException {
		UDPMulticastServer server = new UDPMulticastServer(args);

		server.openMulticastSocket();

		server.openClientSocket();
		server.sendMessage("new_supernode");

		while (true) {
			String message = server.receiveUDPMessage();
			if (message != null) {
				String[] event = message.split(":");
				String eventKey = event[0];
				switch (eventKey) {
					case "resource":
						boolean added = server.superNode.addResource(event[1], Integer.parseInt(event[2]), event[3], event[4]);
						if (added) {
							server.sendResource(server.superNode.indexOfResource(event[4]));
							server.superNode.propagate(false);
						}
						break;
					case "new_supernode":
						server.superNode.propagate(true);
						break;
					case "removed_resource":
						String ip = event[1];
						int port = Integer.parseInt(event[2]);
						boolean removed = true;
						while (removed) {
							removed = false;
							for (int i = 0; i < server.superNode.resourcesHostname.size(); i++) {
								if (i >= server.superNode.resourcesHostname.size())
									break;
								if (server.superNode.resourcesHostname.get(i).equals(ip)
										&& server.superNode.resourcesPort.get(i) == port) {
									server.sendRemovedResorce(i);
									server.superNode.resourcesName.remove(i);
									server.superNode.resourcesHostname.remove(i);
									server.superNode.resourcesPort.remove(i);
									server.superNode.resourcesHashes.remove(i);
									i = 0;
									removed = true;
								}
							}
						}
						break;
				}
			}

			Iterator<String> it = server.superNode.lastCheckedTimeMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Date lastStatus = server.superNode.lastCheckedTimeMap.get(key);
				if (lastStatus == null)
					continue;
				if (new Date().getTime() - lastStatus.getTime() >= 10000) {
					String[] splitedValue = key.split(":");
					String ip = splitedValue[0];
					int port = Integer.parseInt(splitedValue[1]);
					boolean removed = true;
					while (removed) {
						removed = false;
						for (int i = 0; i < server.superNode.resourcesHostname.size(); i++) {
							if (i >= server.superNode.resourcesHostname.size())
								break;
							if (server.superNode.resourcesHostname.get(i).equals(ip)
									&& server.superNode.resourcesPort.get(i) == port) {
								server.sendRemovedResorce(i);
								server.superNode.resourcesName.remove(i);
								server.superNode.resourcesHostname.remove(i);
								server.superNode.resourcesPort.remove(i);
								server.superNode.resourcesHashes.remove(i);
								i = 0;
								removed = true;
							}
						}
					}
					server.superNode.lastCheckedTimeMap.put(key, null);
				}
			}

			if (!server.superNode.propagated) {
				server.superNode.propagate(false);
				System.out.println("Propagating resources");
				server.sendResources();
			}
		}
	}
}
