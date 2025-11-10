public class Main {
  public static void main(String[] args) {
    int port = 9090;
    System.out.println("The server is running on port " + port);
    WebServer webServer = new WebServer(port);
    webServer.go();
    // webServer.stopServer();
    // System.out.println("The Server has been stopped.");

  }
}
