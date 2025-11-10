public class Main {
  public static void main(String[] args) {
    int port = 8080;
    WebServer webServer = new WebServer(port);
    webServer.go();
    webServer.strartServer();
    System.out.println("The server is running on port " + port);
  }
}
