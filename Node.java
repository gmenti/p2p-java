import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.UUID;

public class Node extends UnicastRemoteObject implements NodeInterface {
	String folderPath;

	protected Node() throws RemoteException {
		super();
	}

	public static String getContentFromNode(String ip, int port, String resourceName)
			throws MalformedURLException, RemoteException, NotBoundException {
		String connectLocation = "rmi://" + ip + ":" + port + "/node";
		System.out.println("Connecting to node: " + connectLocation);
		NodeInterface node = (NodeInterface) Naming.lookup(connectLocation);
		return node.getContent(resourceName);
	}

	public static void execute(String[] args) {
		String serverIp = args[1].split(":")[0];
		int serverPort = Integer.parseInt(args[1].split(":")[1]);

		String clientIp = args[2].split(":")[0];
		int clientPort = Integer.parseInt(args[2].split(":")[1]);

		String folderPath = args[3];

		Node node = null;
		try {
			System.setProperty("java.rmi.server.hostname", clientIp);
			LocateRegistry.createRegistry(clientPort);
			System.out.println("Java RMI registry created.");

			node = new Node();
			node.folderPath = folderPath;
			String client = "rmi://" + clientIp + ":" + clientPort + "/node";
			Naming.rebind(client, node);
			System.out.println("Create RMI : " + client);
		} catch (RemoteException | MalformedURLException e) {
			System.out.println("Java RMI registry already exists.");
		}

		try {
			String connectLocation = "rmi://" + serverIp + ":" + serverPort + "/supernode";
			System.out.println("Connecting to server at: " + connectLocation);
			final SuperNodeInterface superNode = (SuperNodeInterface) Naming.lookup(connectLocation);

			File folder = new File(folderPath);
			File[] listOfFiles = folder.listFiles();
			String[] resources = new String[listOfFiles.length];
			String[] hashs = new String[listOfFiles.length];
			int id = 0;
			for (File file : listOfFiles) {
				if (file.isFile()) {
					int newId = id++;
					resources[newId] = file.getName();
					hashs[newId] = UUID.randomUUID().toString();
				}
			}

			superNode.register(clientIp, clientPort, resources, hashs);

			Thread statusThread = new Thread(() -> {
				while (true) {
					try {
						superNode.status(clientIp, clientPort);
						Thread.sleep(5000);
					} catch (RemoteException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			statusThread.start();

			Scanner scanner = new Scanner(System.in);

			System.out.println("Commands: list, show <id>");
			String[] resourcesName;
			String[] hashes;

			while (true) {
				String[] line = scanner.nextLine().split(" ");
				switch (line[0]) {
					case "list":
						resourcesName = superNode.listResources();
						hashes = superNode.listHashes();
						System.out.println(resourcesName.length);
						for (int i = 0; i < resourcesName.length; i++) {
							System.out.println(i + "\t" + resourcesName[i] + "\t" + hashes[i]);
						}
						break;

					case "show":
						resourcesName = superNode.listResources();
						hashs = superNode.listHashes();
						int index = Integer.parseInt(line[1]);
						if (index < 0 || index >= hashs.length)
							break;
						String hash = hashs[index];
						String[] location = superNode.getLocation(hash).split(":");
						System.out.println(getContentFromNode(location[0], Integer.parseInt(location[1]), resourcesName[index]));
						break;
				}
			}

		} catch (Exception e) {
			System.out.println("Connection failed: ");
			e.printStackTrace();
		}
	}

	@Override
	public String getContent(String resourceName) {
		try {
			String content = new String(Files.readAllBytes(Paths.get(folderPath + "/" + resourceName)));
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
