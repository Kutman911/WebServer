import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UserStore {
  private static final String USER_FILE = "users.txt";

  private String simpleHash(String password) {
    return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
  }

  public synchronized boolean registerUser(String username, String password) {
    if (username.isEmpty() || password.isEmpty() || userExists(username)) {
      return false;
    }

    String hashedPassword = simpleHash(password);
    String userData = username + ":" + hashedPassword + "\n";

    try (FileWriter fw = new FileWriter(USER_FILE, true);
    BufferedWriter bw = new BufferedWriter(fw);
    PrintWriter out = new PrintWriter(bw)) {
      out.print(userData);
      return true;
    } catch (IOException e) {
      System.err.println("Error writing to user file: " + e.getMessage());
      return false;
    }
  }

  private boolean userExists(String username) {
    try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith(username + ":")) {
          return true;
        }
      }
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
      System.err.println("Error reading user file: " + e.getMessage());
    }
    return false;
  }

  public boolean authenticateUser(String username, String password) {
    String hashedPassword = simpleHash(password);
    String expectedLine = username + ":" + hashedPassword;

    try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.equals(expectedLine)) {
          return true;
        }
      }
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
      System.err.println("Error reading user file for authentication: " + e.getMessage());
    }
    return false;
  }
}
