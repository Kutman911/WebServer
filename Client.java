import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Client implements Runnable {

    private Socket clientSocket;
    private Thread thread;

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
            String inputLine;
            System.out.println("\n--- Client Request from " + clientSocket.getInetAddress().getHostAddress() + " ---");

            while ((inputLine = reader.readLine()) != null && !inputLine.isEmpty()) {
                System.out.println(inputLine);
            }
            System.out.println("--- End of Request ---\n");

            String message = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Form</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            background: #f0f2f5;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .login-container {
            background: #fff;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 350px;
        }

        .login-container h2 {
            text-align: center;
            margin-bottom: 20px;
            color: #333;
        }

        .form-group {
            margin-bottom: 15px;
        }

        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }

        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 14px;
        }

        input[type="text"]:focus, input[type="password"]:focus {
            border-color: #007bff;
            outline: none;
        }

        .btn {
            width: 100%;
            padding: 10px;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
        }

        .btn:hover {
            background: #0056b3;
        }

        .error {
            color: red;
            font-size: 13px;
            margin-top: 5px;
        }

        .extra-links {
            text-align: center;
            margin-top: 15px;
        }

        .extra-links a {
            color: #007bff;
            text-decoration: none;
        }

        .extra-links a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="login-container">
    <h2>Login</h2>
    <form id="loginForm" novalidate>
        <div class="form-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" required>
            <div class="error" id="usernameError"></div>
        </div>

        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>
            <div class="error" id="passwordError"></div>
        </div>

        <button type="submit" class="btn">Login</button>

        <div class="extra-links">
            <a href="#">Forgot Password?</a> | <a href="#">Sign Up</a>
        </div>
    </form>
</div>

<script>

    document.getElementById('loginForm').addEventListener('submit', function(e) {
        e.preventDefault();

        let valid = true;


        document.getElementById('usernameError').textContent = '';
        document.getElementById('passwordError').textContent = '';

        const username = document.getElementById('username').value.trim();
        if (username === '') {
            document.getElementById('usernameError').textContent = 'Username is required';
            valid = false;
        }


        const password = document.getElementById('password').value.trim();
        if (password === '') {
            document.getElementById('passwordError').textContent = 'Password is required';
            valid = false;
        }


        if (valid) {
            alert('Login successful (demo)');
        }
    });
</script>

</body>
</html>
""";

            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            int fileLength = data.length;

            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Server: Java HTTP Server from Intern Labs 7.0 - Java Backend Developer");
            printWriter.println("Content-Type: text/html; charset=utf-8");
            printWriter.println("Content-Length: " + fileLength);
            printWriter.println();
            printWriter.flush();

            dataOut.write(data, 0, fileLength);
            dataOut.flush();

        } catch (IOException ioe) {
            System.out.println("Connection handler error: " + ioe.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public void go() {
        thread.start();
    }
}
