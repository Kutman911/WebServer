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
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
    BufferedOutputStream dataOut = new BufferedOutputStream(out)
    ) {

      String firstLine = reader.readLine();
      if (firstLine == null) {
        return;
      }

      String[] parts = firstLine.split(" ");
      if (parts.length < 2) return;
      String method = parts[0];
      String path = parts[1];

      System.out.println("\n--- Request: " + method + " " + path + " from " + clientSocket.getInetAddress().getHostAddress());

      int contentLength = 0;
      String cookieHeader = null;
      String headerLine;
      while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
        if (headerLine.startsWith("Content-Length:")) {
          contentLength = Integer.parseInt(headerLine.substring(15).trim());
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
      String redirectLocation = "/";
      String setCookieHeaderValue = null;


      if ("/".equals(path)) {
        responseBody = getIndexContent(sessionUser);

      } else if ("/register".equals(path) && "GET".equalsIgnoreCase(method)) {
        responseBody = getHtmlContent("register.html")
        .replace("<div class=\"error-message\" style=\"display: none;\">", "<div class=\"error-message\" style=\"display: none;\">")
        .replace("value=\"\"", "value=\"\"");

      } else if ("/register".equals(path) && "POST".equalsIgnoreCase(method)) {
        String username = postData.getOrDefault("username", "").trim();
        String password = postData.getOrDefault("password", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
          responseBody = getHtmlContent("register.html")
          .replace("<div class=\"error-message\" style=\"display: none;\">", "<div class=\"error-message\">Заполните все поля.</div>")
          .replace("value=\"\"", "value=\"" + escapeHtml(username) + "\"");
        } else if (userStore.registerUser(username, password)) {
          statusCode = "302 Found";
          isRedirect = true;
          redirectLocation = "/login";
        } else {
          responseBody = getHtmlContent("register.html")
          .replace("<div class=\"error-message\" style=\"display: none;\">", "<div class=\"error-message\">Пользователь с таким именем уже существует.</div>")
          .replace("value=\"\"", "value=\"" + escapeHtml(username) + "\"");
        }

      } else if ("/login".equals(path) && "GET".equalsIgnoreCase(method)) {
        responseBody = getHtmlContent("login.html")
        .replace("<div class=\"error-message\" style=\"display: none;\">", "<div class=\"error-message\" style=\"display: none;\">");

      } else if ("/login".equals(path) && "POST".equalsIgnoreCase(method)) {
        String username = postData.getOrDefault("username", "").trim();
        String password = postData.getOrDefault("password", "").trim();

        if (userStore.authenticateUser(username, password)) {
          activeSessions.put(username, System.currentTimeMillis());
          setCookieHeaderValue = SESSION_COOKIE_NAME + "=" + username + "; Max-Age=3600; Path=/; HttpOnly";
          statusCode = "302 Found";
          isRedirect = true;
          redirectLocation = "/test";
        } else {
          responseBody = getHtmlContent("login.html")
          .replace("<div class=\"error-message\" style=\"display: none;\">", "<div class=\"error-message\">Неверное имя пользователя или пароль.</div>");
        }

      } else if (path.startsWith("/test") && "GET".equalsIgnoreCase(method)) {
        if (sessionUser == null) {
          statusCode = "302 Found";
          isRedirect = true;
          redirectLocation = "/login";
        } else {
          String testId = path.contains("b") ? "b" : "a";
          TestQuestions.Test test = TestQuestions.getTest(testId);
          if (test == null) {
            responseBody = "<h1>Тест не найден</h1>";
          } else {
            responseBody = generateTestFormHtml(test);
          }
        }

      } else if (path.startsWith("/test") && "POST".equalsIgnoreCase(method)) {
        if (sessionUser == null) {
          statusCode = "302 Found";
          isRedirect = true;
          redirectLocation = "/login";
        } else {
          String testId = postData.getOrDefault("test_id", "a");
          Map<String, String> answers = new HashMap<>();
          postData.forEach((k, v) -> {
            if (k.startsWith("q")) answers.put(k, v);
          });

          int score = calculateScore(testId, answers);
          userStore.saveTestResult(sessionUser, testId, score);

          TestQuestions.Test test = TestQuestions.getTest(testId);
          int total = test != null ? test.questions.size() : 10;
          String comment = score >= 9 ? "Прекрасно!" :
          score >= 7 ? "Отличный результат!" :
          score >= 5 ? "Хороший результат!" :
          "Есть куда расти, продолжайте тренироваться!";

          responseBody = getHtmlContent("test_result.html")
          .replace("{{username}}", escapeHtml(sessionUser))
          .replace("4 / 10", score + " / " + total)
          .replace("Ваш результат показывает, что у вас есть потенциал для развития логического мышления. Продолжайте тренироваться!", comment);
        }

      } else if ("/results".equals(path)) {
        if (sessionUser == null) {
          statusCode = "302 Found";
          isRedirect = true;
          redirectLocation = "/login";
        } else {
          responseBody = generateResultsPage(sessionUser);
        }

      } else if ("/logout".equals(path)) {
        if (sessionUser != null) {
          activeSessions.remove(sessionUser);
        }
        setCookieHeaderValue = SESSION_COOKIE_NAME + "=; Max-Age=0; Path=/";
        statusCode = "302 Found";
        isRedirect = true;
        redirectLocation = "/";

      } else {
        statusCode = "404 Not Found";
        responseBody = "<h1>404 — Страница не найдена</h1><a href=\"/\">На главную</a>";
      }

      if (!isRedirect) {
        byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        sendResponseHeader(printWriter, statusCode, contentType, bodyBytes.length, false, null, setCookieHeaderValue);
        dataOut.write(bodyBytes);
        dataOut.flush();
      } else {
        sendResponseHeader(printWriter, statusCode, contentType, 0, true, redirectLocation, setCookieHeaderValue);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private String getIndexContent(String sessionUser) {
    String html = getHtmlContent("index.html");

    if (sessionUser != null) {
      String loggedInMenu = "<div class=\"user-status\">" +
      "<p>Вы вошли как: <strong>" + escapeHtml(sessionUser) + "</strong></p>" +
      "<ul>" +
      "<li><a href=\"/test\" class=\"menu-link\">Пройти Тест A</a></li>" +
      "<li><a href=\"/test?b\" class=\"menu-link\">Пройти Тест B</a></li>" +
      "<li><a href=\"/results\" class=\"menu-link\">Мои результаты</a></li>" +
      "<li><a href=\"/logout\" class=\"menu-link\">Выйти</a></li>" +
      "</ul></div>";

      return html.replace("<p id=\"user-status\" class=\"user-status\"></p>", loggedInMenu);
    } else {
      return html.replace("<p id=\"user-status\" class=\"user-status\"></p>",
      "<p class=\"user-status\">Вы не авторизованы</p>");
    }
  }

  private String generateTestFormHtml(TestQuestions.Test test) {
    String template = getHtmlContent("test_form.html");
    StringBuilder questionsHtml = new StringBuilder();

    questionsHtml.append("<input type=\"hidden\" name=\"test_id\" value=\"").append(test.id).append("\">");

    for (TestQuestions.Question q : test.questions) {
      questionsHtml.append("<div class=\"question-card\">");
      questionsHtml.append("<h2>").append(escapeHtml(q.text)).append("</h2>");
      questionsHtml.append("<div class=\"options\">");

      for (Map.Entry<String, String> opt : q.options.entrySet()) {
        String key = opt.getKey();
        String value = opt.getValue();

        String labelText = q.isTrueFalse ? value : key.toUpperCase() + ") " + value;

        questionsHtml.append("<label>");
        questionsHtml.append("<input type=\"radio\" name=\"").append(q.id)
        .append("\" value=\"").append(key).append("\" required>");
        questionsHtml.append(" ").append(escapeHtml(labelText));
        questionsHtml.append("</label>");
      }
      questionsHtml.append("</div></div>");
    }

    String result = template
    .replaceAll("(?s)<div class=\"empty-message\"[^>]*>.*?</div>", questionsHtml.toString())
    .replace("<h1>IQ Тест</h1>", "<h1>" + escapeHtml(test.title) + "</h1>")
    .replace("<p class=\"subtitle\">Ответьте на 10 вопросов.*?</p>",
    "<p class=\"subtitle\">Ответьте на 10 вопросов. Все поля обязательны для заполнения.</p>");

    return result;
  }

  private String generateResultsPage(String username) {
    Map<String, Integer> results = userStore.getTestResults(username);
    if (results.isEmpty()) {
      return "<div style=\"text-align:center; padding:50px;\"><h2>Результатов пока нет</h2><a href=\"/test\">Пройти тест</a></div>";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\"><title>Результаты</title>");
    sb.append("<style>body{font-family:sans-serif;background:#f3f4f6;padding:40px;}table{width:80%;margin:0 auto;border-collapse:collapse;background:white;}th,td{border:1px solid #ddd;padding:12px;text-align:center;}th{background:#1d4ed8;color:white;}</style></head><body>");
    sb.append("<h1 style=\"text-align:center;\">Результаты пользователя: <strong>").append(escapeHtml(username)).append("</strong></h1>");
    sb.append("<table><thead><tr><th>Тест</th><th>Результат</th></tr></thead><tbody>");

    results.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
      String name = e.getKey().equals("a") ? "IQ Тест A: Классика и Ловушки" : "IQ Тест B: Смекалка и Внимание";
      sb.append("<tr><td>").append(escapeHtml(name)).append("</td><td><strong>").append(e.getValue()).append("/10</strong></td></tr>");
    });

    sb.append("</tbody></table><div style=\"text-align:center;margin-top:30px;\">");
    sb.append("<a href=\"/test\" style=\"padding:10px 20px;background:#1d4ed8;color:white;border-radius:8px;text-decoration:none;\">Пройти ещё тест</a> ");
    sb.append("<a href=\"/\" style=\"margin-left:20px;\">На главную</a></div></body></html>");

    return sb.toString();
  }

  private int calculateScore(String testId, Map<String, String> answers) {
    TestQuestions.Test test = TestQuestions.getTest(testId);
    if (test == null) {
      return 0;
    }
    int score = 0;
    for (TestQuestions.Question q : test.questions) {
      String userAnswer = answers.get(q.id);
      if (userAnswer != null && userAnswer.equals(q.correctAnswerKey)) {
        score++;
      }
    }
    return score;
  }

  private Map<String, String> readPostData(BufferedReader reader, int contentLength) throws IOException {
    char[] buffer = new char[contentLength];
    int read = reader.read(buffer, 0, contentLength);
    String body = new String(buffer, 0, read);

    Map<String, String> data = new HashMap<>();
    if (!body.isEmpty()) {
      String[] pairs = body.split("&");
      for (String pair : pairs) {
        String[] kv = pair.split("=", 2);
        if (kv.length == 2) {
          String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
          String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
          data.put(key, value);
        }
      }
    }
    return data;
  }

  private String getCookieValue(String header, String name) {
    if (header == null) {
      return null;
    }
    for (String cookie : header.split(";")) {
      String trimmed = cookie.trim();
      if (trimmed.startsWith(name + "=")) {
        return trimmed.substring(name.length() + 1);
      }
    }
    return null;
  }

  private String getHtmlContent(String fileName) {
    String path = "resources/" + fileName;
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
      }
    } catch (IOException e) {
      System.err.println("Не найден файл: " + path);
      return "<h1>500 — Шаблон не найден: " + fileName + "</h1>";
    }
    return sb.toString();
  }

  private void sendResponseHeader(PrintWriter pw, String status, String cType, int length, boolean redirect, String location, String cookie) {
    pw.println("HTTP/1.1 " + status);
    pw.println("Server: Java-IQ-Platform/1.0");
    if (cookie != null) {
      pw.println("Set-Cookie: " + cookie);
    }
    if (redirect) {
      pw.println("Location: " + location);
    } else {
      pw.println("Content-Type: " + cType);
      pw.println("Content-Length: " + length);
    }
    pw.println();
    pw.flush();
  }

  private String escapeHtml(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  public void go() {
    thread.start();
  }
}
