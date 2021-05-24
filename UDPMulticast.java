import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class UDPMulticast {
	protected String multicastAddress;
	protected int multicastPort;
	protected DatagramSocket clientSocket;
	protected MulticastSocket multicastSocket;
	protected InetAddress group;

	public UDPMulticast(String[] args) {
		this.multicastAddress = args[0];
		this.multicastPort = Integer.parseInt(args[1]);
	}

	public void openClientSocket() throws SocketException {
		this.clientSocket = new DatagramSocket();
	}

	public void closeClientSocket() {
		this.clientSocket.close();
	}

	public void openMulticastSocket() throws IOException {
		multicastSocket = new MulticastSocket(multicastPort);
		group = InetAddress.getByName(multicastAddress);
		multicastSocket.joinGroup(group);
		multicastSocket.setSoTimeout(100);
	}

	public void closeMulticastSocket() {
		try {
			multicastSocket.leaveGroup(group);
			multicastSocket.close();
		} catch (IOException e) {
			//
		}
	}

	public void sendMessage(String message) throws IOException {
		InetAddress group = InetAddress.getByName(multicastAddress);
		byte[] msg = message.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length, group, multicastPort);
		clientSocket.send(packet);
	}

	public String receiveUDPMessage() throws IOException {
		byte[] buffer = new byte[1024];

		// System.out.println("Waiting for multicast message...");
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		try {
			multicastSocket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			// System.out.println("[Multicast UDP message received] >> " + msg);
			return msg;
		} catch (Exception e) {
			return null;
		} finally {
			
		}
	}
}
