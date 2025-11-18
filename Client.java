import java.net.Socket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.io.File;

public class Client implements Runnable {

  private Socket clientSocket;
  private Thread thread;
  private static final UserStore userStore = new UserStore();

  public Client(Socket clientSocket) {
    this.clientSocket = clientSocket;
    thread = new Thread(this);
  }

  public void run() {
    try (
    InputStream in = clientSocket.getInputStream();
    OutputStream out = clientSocket.getOutputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    PrintWriter printWriter = new PrintWriter(out, true);
    BufferedOutputStream dataOut = new BufferedOutputStream(out);
    ) {

      String firstLine = reader.readLine();
      if (firstLine == null) return;

      String[] parts = firstLine.split(" ");
      String method = parts[0];
      String path = parts[1];

      System.out.println("\n--- Client Request from " + clientSocket.getInetAddress().getHostAddress() + " ---");
      System.out.println("Method: " + method + ", Path: " + path);

      int contentLength = 0;
      String headerLine;
      while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
        if (headerLine.startsWith("Content-Length:")) {
          contentLength = Integer.parseInt(headerLine.substring(16).trim());
        }
      }

      Map<String, String> postData = new HashMap<>();
      if ("POST".equalsIgnoreCase(method) && contentLength > 0) {
        postData = readPostData(reader, contentLength);
      }

      String sessionUser = null;

      String responseBody = "";
      String statusCode = "200 OK";
      String contentType = "text/html; charset=utf-8";
      boolean isRedirect = false;


      if ("/".equals(path)) {
        responseBody = getHtmlContent("index.html");

      } else if ("/register".equals(path) && "GET".equalsIgnoreCase(method)) {
        responseBody = getHtmlContent("register.html")
        .replace("", "")
        .replace("", "");

      } else if ("/register".equals(path) && "POST".equalsIgnoreCase(method)) {
        String username = postData.getOrDefault("username", "").trim();
        String password = postData.getOrDefault("password", "").trim();

        if (userStore.registerUser(username, password)) {
          statusCode = "302 Found";
          isRedirect = true;
          sendResponseHeader(printWriter, statusCode, contentType, 0, isRedirect, "/login");
          return;
        } else {
          String error = "<p class='error-message'>Ошибка регистрации. Проверьте поля или пользователь с таким именем уже существует.</p>";
          responseBody = getHtmlContent("register.html")
          .replace("", error)
          .replace("", username);
        }

      } else if ("/login".equals(path) && "GET".equalsIgnoreCase(method)) {
        responseBody = getHtmlContent("login.html")
        .replace("", "");

      } else if ("/login".equals(path) && "POST".equalsIgnoreCase(method)) {
        String username = postData.getOrDefault("username", "").trim();
        String password = postData.getOrDefault("password", "").trim();

        if (userStore.authenticateUser(username, password)) {
          System.out.println("User authenticated: " + username);

          statusCode = "302 Found";
          isRedirect = true;
          sendResponseHeader(printWriter, statusCode, contentType, 0, isRedirect, "/");
          return;
        } else {
          String error = "<p class='error-message'>Ошибка: Неверное имя пользователя или пароль.</p>";
          responseBody = getHtmlContent("login.html")
          .replace("", error);
        }

      } else {
        statusCode = "404 Not Found";
        responseBody = "<h1>404 Not Found</h1><p>Запрашиваемый ресурс не найден.</p>";
      }

      byte[] data = responseBody.getBytes(StandardCharsets.UTF_8);
      sendResponseHeader(printWriter, statusCode, contentType, data.length, isRedirect, null);
      dataOut.write(data, 0, data.length);
      dataOut.flush();

      } catch (IOException ioe) {
      if (!ioe.getMessage().contains("Connection reset") && !ioe.getMessage().contains("Socket closed")) {
        System.out.println("Connection handler error: " + ioe.getMessage());
      }
    } finally {
      try {
        if (clientSocket != null && !clientSocket.isClosed()) {
          clientSocket.close();
        }
      } catch (IOException e) {
      }
    }
  }

  private String getHtmlContent(String fileName) {
    String path = "resources/" + fileName;
    StringBuilder content = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        content.append(line).append("\n");
      }
    } catch (IOException e) {
      System.err.println("Error reading HTML file: " + path + " | " + e.getMessage());
      return "<h1>500 Internal Server Error</h1><p>Template file not found: " + fileName + "</p>";
    }
    return content.toString();
  }

  private Map<String, String> readPostData(BufferedReader reader, int contentLength) throws IOException {
    char[] charBuffer = new char[contentLength];
    reader.read(charBuffer, 0, contentLength);
    String postBody = new String(charBuffer);

    Map<String, String> data = new HashMap<>();
    String[] pairs = postBody.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      if (keyValue.length == 2) {
        try {
          String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
          String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
          data.put(key, value);
        } catch (UnsupportedEncodingException e) {
        }
      }
    }
    return data;
  }

  private void sendResponseHeader(PrintWriter printWriter, String statusCode, String contentType, int contentLength, boolean isRedirect, String location) {
    printWriter.println("HTTP/1.1 " + statusCode);
    printWriter.println("Server: Java HTTP Server - Intern Labs");
    if (!isRedirect) {
      printWriter.println("Content-Type: " + contentType);
      printWriter.println("Content-Length: " + contentLength);
    } else {
      printWriter.println("Location: " + location);
    }
    printWriter.println();
    printWriter.flush();
  }

  public void go() {
    thread.start();
  }
}
