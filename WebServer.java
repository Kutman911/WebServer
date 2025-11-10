import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class WebServer {
  private ServerSocket serverSocket;
  private Object lock;
  private boolean flag;
  private Thread thread;

  public WebServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      lock = new Object;
      flag = true;
      thread = new Thread(this);
    } catch (IOException ioe) {
      System.out.println("Server start error " + ioe);
    }
  }

  public void strartServer() {
    while(flag) {
      try {
        Socket socket = serverSocket.accept();
        synchronized(lock) {
          Client client  = new Client(socket);
          client.go();
        }
      } catch (IOException ioe) {
        System.out.println("Server error " + ioe);
      }
    }
  }

  public void run() {
    Scanner scanner = new Scanner(System.in);
    String command = scanner.nextLine();

    if(command.equals("stop")) {
      stopServer(); 
    }
  }
  public void go() {
    thread.start();
  }

  public void stopServer() {
    flag = false;
  }
}
