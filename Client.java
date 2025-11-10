import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;
public class Client implements Runnable {

  private Socket clientSocket;
  private Thread thread;

  public Client(Socket clientSocket) {
    this.clientSocket = clientSocket;
    InputStream in = null;
    try {
      in = clientSocket.getInputStream();
    } catch(IOException ioe) {
      System.out.println("Error into Input Stream " + ioe);
    }
    thread = new Thread(this);
  }

  public void run() {
    System.out.println("Processing client.");
    System.out.println(clientSocket);
    while(true) {
      try {
        int unicode = in.read();
        char symbol = (char)unicode;
        System.out.println(symbol);
      } catch(IOException ioe) {
        System.out.println("Error " + ioe);
      }
    }
  }

  public void go() {
    thread.start();
  }
}
