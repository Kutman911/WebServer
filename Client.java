import java.net.Socket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;

public class Client implements Runnable {

  private Socket clientSocket;
  private Thread thread;
  private static final UserStore userStore = new UserStore();

  private static final Map<String, Long> activeSessions = new HashMap<>();
  private static final String SESSION_COOKIE_NAME = "session_user";

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
      String cookieHeader = null;
      String headerLine;
      while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
        if (headerLine.startsWith("Content-Length:")) {
          contentLength = Integer.parseInt(headerLine.substring(16).trim());
        } else if (headerLine.startsWith("Cookie:")) {
          cookieHeader = headerLine.substring(8).trim();
        }
      }

      String sessionUser = getCookieValue(cookieHeader, SESSION_COOKIE_NAME);

      if (sessionUser != null && !activeSessions.containsKey(sessionUser)) {
        sessionUser = null;
      }

      Map<String, String> postData = new HashMap<>();
      if ("POST".equalsIgnoreCase(method) && contentLength > 0) {
        postData = readPostData(reader, contentLength);
      }

      String responseBody = "";
      String statusCode = "200 OK";
      String contentType = "text/html; charset=utf-8";
      boolean isRedirect = false;


      if ("/".equals(path)) {
        responseBody = getHtmlContent("index.html");
        if (sessionUser != null) {
          responseBody = responseBody.replace(
          "id=\"user-status\"></p>",
          "id=\"user-status\">Вы вошли как: **" + sessionUser + "** (<a href='/logout'>Выход</a>)</p>");
        }

      } else if ("/register".equals(path) && "GET".equalsIgnoreCase(method)) {
        responseBody = getHtmlContent("register.html")
        .replace("<!-- ERROR_PLACEHOLDER -->", "")
        .replace("<!-- USERNAME_PLACEHOLDER -->", "");

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
          .replace("<!-- ERROR_PLACEHOLDER -->", error)
          .replace("<!-- USERNAME_PLACEHOLDER -->", username);
        }

      } else if ("/login".equals(path) && "GET".equalsIgnoreCase(method)) {
        responseBody = getHtmlContent("login.html")
        .replace("<!-- ERROR_PLACEHOLDER -->", "");

      } else if ("/login".equals(path) && "POST".equalsIgnoreCase(method)) {
        String username = postData.getOrDefault("username", "").trim();
        String password = postData.getOrDefault("password", "").trim();

        if (userStore.authenticateUser(username, password)) {
          System.out.println("User authenticated: " + username);

          activeSessions.put(username, System.currentTimeMillis());

          statusCode = "302 Found";
          isRedirect = true;

          printWriter.println("HTTP/1.1 " + statusCode);
          printWriter.println("Server: Java HTTP Server - Intern Labs");
          setCookie(printWriter, SESSION_COOKIE_NAME, username, 3600);
          printWriter.println("Location: /");
          printWriter.println();
          printWriter.flush();
          return;

        } else {
          String error = "<p class='error-message'>Ошибка: Неверное имя пользователя или пароль.</p>";
          responseBody = getHtmlContent("login.html")
          .replace("<!-- ERROR_PLACEHOLDER -->", error);
        }

      } else if ("/logout".equals(path) && "GET".equalsIgnoreCase(method)) {
        if (sessionUser != null) {
          activeSessions.remove(sessionUser);
        }

        statusCode = "302 Found";
        isRedirect = true;
        printWriter.println("HTTP/1.1 " + statusCode);
        printWriter.println("Server: Java HTTP Server - Intern Labs");
        setCookie(printWriter, SESSION_COOKIE_NAME, "deleted", 0);
        printWriter.println("Location: /");
        printWriter.println();
        printWriter.flush();
        return;

      } else if ("/test".equals(path) && "GET".equalsIgnoreCase(method)) {
        if (sessionUser == null) {
          statusCode = "302 Found";
          isRedirect = true;
          sendResponseHeader(printWriter, statusCode, contentType, 0, isRedirect, "/login");
          return;
        }
        responseBody = getHtmlContent("test_form.html");

      } else if ("/test".equals(path) && "POST".equalsIgnoreCase(method)) {
        if (sessionUser == null) {
          statusCode = "302 Found";
          isRedirect = true;
          sendResponseHeader(printWriter, statusCode, contentType, 0, isRedirect, "/login");
          return;
        }

        int score = calculateScore(postData);
        String message;

        if (score == 3) {
          message = "Великолепный результат! Вы ответили на все вопросы правильно.";
        } else if (score > 0) {
          message = "Хороший результат! Но можно и лучше.";
        } else {
          message = "Попробуйте еще раз. Некоторые вопросы были не очень сложными.";
        }

        responseBody = getHtmlContent("test_result.html")
        .replace("<!-- USERNAME_PLACEHOLDER -->", sessionUser)
        .replace("<!-- SCORE_PLACEHOLDER -->", String.valueOf(score))
        .replace("<!-- MESSAGE_PLACEHOLDER -->", message);

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

  private String getCookieValue(String header, String name) {
    if (header == null) return null;
    String[] cookies = header.split(";");
    for (String cookie : cookies) {
      String trimmedCookie = cookie.trim();
      if (trimmedCookie.startsWith(name + "=")) {
        return trimmedCookie.substring(name.length() + 1);
      }
    }
    return null;
  }

  private void setCookie(PrintWriter printWriter, String name, String value, int maxAgeSeconds) {
    String cookieHeader = name + "=" + value + "; Max-Age=" + maxAgeSeconds + "; Path=/";
    printWriter.println("Set-Cookie: " + cookieHeader);
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
    int bytesRead = reader.read(charBuffer, 0, contentLength);
    if (bytesRead == -1) return new HashMap<>();

    String postBody = new String(charBuffer, 0, bytesRead);

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

  private int calculateScore(Map<String, String> answers) {
    int score = 0;

    if ("b".equals(answers.get("q1"))) {
      score++;
    }
    if ("b".equals(answers.get("q2"))) {
      score++;
    }
    if ("b".equals(answers.get("q3"))) {
      score++;
    }
    return score;
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
