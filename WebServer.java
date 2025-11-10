import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class WebServer implements Runnable {
  private ServerSocket serverSocket;
  private Object lock;
  private volatile boolean flag;
  private Thread thread;

  public WebServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      lock = new Object();
      flag = true;
      thread = new Thread(this);
    } catch (IOException ioe) {
      System.out.println("Server start error " + ioe);
    }
  }

  @Override
  public void run() {
    System.out.println("Server listening on port " + serverSocket.getLocalPort());
    while(flag) {
      try {
        Socket socket = serverSocket.accept();
        System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
        Client client = new Client(socket);
        client.go();

      } catch (IOException ioe) {
          if (flag) {
            System.out.println("Server error " + ioe);
        }
      }
    }
    System.out.println("Server stopped accepting connections.");
  }

  public void go() {
    thread.start();
    startConsoleListener();
  }

  private void startConsoleListener() {
    new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      System.out.println("Type 'stop' to shut down the server.");

      while(true) {
        String command = scanner.nextLine();
        if(command.trim().equalsIgnoreCase("stop")) {
          stopServer();
          scanner.close();
          break;
        }
      }
    }).start();
  }

  public void stopServer() {
    flag = false;
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
      System.out.println("Error closing ServerSocket: " + e.getMessage());
    }
    System.out.println("Attempting graceful shutdown...");
  }
}
