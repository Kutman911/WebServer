import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
* Class for storing user's data and their results in distintc files
*/
public class UserStore {
  private static final String DATA_DIR = "user_data";

  public UserStore() {
    File dir = new File(DATA_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  /**
  * Returns object File for user's data
  */
  private File getUserFile(String username) {
    String safeUsername = username.replaceAll("[^a-zA-Z0-9_.-]", "_");
    return new File(DATA_DIR, safeUsername + ".data");
  }

  private String simpleHash(String password) {
    return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
  }

  /**
  * Loads user's data from its file
  */
  private synchronized Properties loadUserData(String username) {
    File userFile = getUserFile(username);
    Properties props = new Properties();
    if (userFile.exists()) {
      try (FileInputStream fis = new FileInputStream(userFile)) {
        props.load(fis);
        return props;
      } catch (IOException e) {
        System.err.println("Error reading user data for " + username + ": " + e.getMessage());
        return null;
      }
    }
    return null;
  }

  /**
  * Saves user's data to its file.
  */
  private synchronized void saveUserData(String username, Properties props) {
    File userFile = getUserFile(username);
    try (FileOutputStream fos = new FileOutputStream(userFile)) {
      props.store(fos, "User data for " + username);
    } catch (IOException e) {
      System.err.println("Error writing user data for " + username + ": " + e.getMessage());
    }
  }

  public boolean registerUser(String username, String password) {
    if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
      return false;
    }

    if (loadUserData(username) != null) {
      return false;
    }

    String hashedPassword = simpleHash(password);
    Properties props = new Properties();
    props.setProperty("password_hash", hashedPassword);

    saveUserData(username, props);
    return true;
  }

  public boolean authenticateUser(String username, String password) {
    Properties props = loadUserData(username);
    if (props == null) {
      return false;
    }

    String storedHash = props.getProperty("password_hash");
    String inputHash = simpleHash(password);

    return storedHash != null && storedHash.equals(inputHash);
  }

  /**
  * Save Test's result
  */
  public void saveTestResult(String username, String testId, int score) {
    Properties props = loadUserData(username);
    if (props == null) {
      System.err.println("Attempted to save result for non-existent user: " + username);
      return;
    }

    props.setProperty("test_" + testId, String.valueOf(score));

    saveUserData(username, props);
  }
  
  public Map<String, Integer> getTestResults(String username) {
    Map<String, Integer> results = new HashMap<>();
    Properties props = loadUserData(username);

    if (props != null) {
      for (String key : props.stringPropertyNames()) {
        if (key.startsWith("test_")) {
          String testId = key.substring(5);
          try {
            int score = Integer.parseInt(props.getProperty(key));
            results.put(testId, score);
          } catch (NumberFormatException e) {
            System.err.println("Invalid score format in file for user " + username + " under key " + key);
          }
        }
      }
    }
    return results;
  }
}
