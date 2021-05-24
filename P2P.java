import java.io.IOException;

public class P2P {

  public static void main(String args[]) throws IOException {
    if (args[0].equals("node")) {
      for (int i = 0; i < args.length; i++) {
        System.out.println(args[i]);
      }
      Node.execute(args);
    } else if (args[0].equals("supernode")) {
      UDPMulticastServer.execute(args);
    }
  }
}
