package com.example.codeee.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@Service
public class ExecutionService {

    public String runCode(String language, String code, String input) {

        // ✅ ALWAYS use container path (NOT Windows path)
        String folderName = "/workspace/code-exec";

        try {
            Path dir = Path.of(folderName);

            // 🔥 delete old files
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .map(Path::toFile)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(File::delete);
            }

            // ✅ create folder
            Files.createDirectories(dir);

            // ✅ write code file
            String fileName = getFileName(language);
            Path filePath = Path.of(folderName, fileName);
            Files.writeString(filePath, code);

            // ✅ DEBUG (IMPORTANT)
            System.out.println("File created: " + filePath);

            ProcessBuilder pb;

            switch (language) {

                case "python":

                    String pyContainer = "py-run-" + System.currentTimeMillis();

                    // 1️⃣ Create container
                    new ProcessBuilder(
                            "docker", "run", "-d",
                            "--name", pyContainer,
                            "python:3.9",
                            "sleep", "10"
                    ).start().waitFor();

                    // 2️⃣ Copy file into container
                    new ProcessBuilder(
                            "docker", "cp",
                            folderName + "/script.py",
                            pyContainer + ":/script.py"
                    ).start().waitFor();

                    // 3️⃣ Execute Python
                    pb = new ProcessBuilder(
                            "docker", "exec", "-i",
                            pyContainer,
                            "python", "/script.py"
                    );

                    // 4️⃣ CLEANUP after execution (IMPORTANT)
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            new ProcessBuilder("docker", "rm", "-f", pyContainer).start();
                        } catch (Exception ignored) {}
                    }).start();

                    break;


                case "java":

                    // 1. Create container
                    String containerName = "java-run-" + System.currentTimeMillis();

                    new ProcessBuilder("docker", "run", "-d", "--name", containerName, "eclipse-temurin:17", "sleep", "5").start().waitFor();

                    // 2. Copy file into container
                    new ProcessBuilder("docker", "cp",
                            folderName + "/Main.java",
                            containerName + ":/Main.java"
                    ).start().waitFor();

                    // 3. Execute code
                    pb = new ProcessBuilder(
                            "docker", "exec", containerName,
                            "sh", "-c",
                            "javac /Main.java && java -cp / Main"
                    );

                    break;


                case "c":

                    String cContainer = "c-run-" + System.currentTimeMillis() + "-" + Math.random();

                    // 🔥 Remove if exists
                    new ProcessBuilder("docker", "rm", "-f", cContainer)
                            .start().waitFor();

                    // 1️⃣ Create container
                    new ProcessBuilder("docker", "run", "-d",
                            "--name", cContainer,
                            "gcc:latest",
                            "sleep", "10")
                            .start().waitFor();

                    // 2️⃣ Copy file
                    new ProcessBuilder("docker", "cp",
                            folderName + "/main.c",
                            cContainer + ":/main.c")
                            .start().waitFor();

                    // 3️⃣ Execute
                    pb = new ProcessBuilder(
                            "docker", "exec", "-i",
                            cContainer,
                            "sh", "-c",
                            "gcc /main.c -o /main && /main"
                    );

                    // 4️⃣ Cleanup
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            new ProcessBuilder("docker", "rm", "-f", cContainer).start();
                        } catch (Exception ignored) {}
                    }).start();

                    break;


                case "cpp":

                    String cppContainer = "cpp-run-" + System.currentTimeMillis() + "-" + Math.random();

                    // 🔥 Remove if exists
                    new ProcessBuilder("docker", "rm", "-f", cppContainer)
                            .start().waitFor();

                    // 1️⃣ Create container
                    new ProcessBuilder("docker", "run", "-d",
                            "--name", cppContainer,
                            "gcc:latest",
                            "sleep", "10")
                            .start().waitFor();

                    // 2️⃣ Copy file
                    new ProcessBuilder("docker", "cp",
                            folderName + "/main.cpp",
                            cppContainer + ":/main.cpp")
                            .start().waitFor();

                    // 3️⃣ Execute
                    pb = new ProcessBuilder(
                            "docker", "exec", "-i",
                            cppContainer,
                            "sh", "-c",
                            "g++ /main.cpp -o /main && /main"
                    );


                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            new ProcessBuilder("docker", "rm", "-f", cppContainer).start();
                        } catch (Exception ignored) {}
                    }).start();

                    break;


                case "js":

                    String jsContainer = "js-run-" + System.currentTimeMillis() + "-" + Math.random();

                    // 🔥 Remove if exists
                    new ProcessBuilder("docker", "rm", "-f", jsContainer)
                            .start().waitFor();

                    // 1️⃣ Create container
                    new ProcessBuilder("docker", "run", "-d",
                            "--name", jsContainer,
                            "node:18",
                            "sleep", "10")
                            .start().waitFor();

                    // 2️⃣ Copy file
                    new ProcessBuilder("docker", "cp",
                            folderName + "/script.js",
                            jsContainer + ":/script.js")
                            .start().waitFor();

                    // 3️⃣ Execute
                    pb = new ProcessBuilder(
                            "docker", "exec", "-i",
                            jsContainer,
                            "node", "/script.js"
                    );

                    // 4️⃣ Cleanup
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            new ProcessBuilder("docker", "rm", "-f", jsContainer).start();
                        } catch (Exception ignored) {}
                    }).start();

                    break;


                default:
                    return "Unsupported language";
            }

            Process process = pb.start();

            // ✅ FIX INPUT (IMPORTANT)
            if (input != null && !input.isEmpty()) {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream())
                );
                writer.write(input + "\n");  // 🔥 FIXED
                writer.flush();
                writer.close();
            }

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Error: Timeout";
            }

            BufferedReader output = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            BufferedReader error = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            StringBuilder result = new StringBuilder();
            String line;

            while ((line = output.readLine()) != null) {
                result.append(line).append("\n");
            }

            while ((line = error.readLine()) != null) {
                result.append(line).append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getFileName(String language) {
        switch (language) {
            case "python": return "script.py";
            case "java": return "Main.java";
            case "c": return "main.c";
            case "cpp": return "main.cpp";
            case "js": return "script.js";
            default: return "file.txt";
        }
    }
}
