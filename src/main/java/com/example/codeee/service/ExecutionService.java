package com.example.codeee.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ExecutionService {

    // ✅ FULL DOCKER PATH (IMPORTANT)
    private static final String DOCKER = "C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe";

    public String runCode(String language, String code, String input) {

        String folderName = System.getProperty("java.io.tmpdir") + "code-" + UUID.randomUUID();

        try {
            Files.createDirectories(Path.of(folderName));

            String dockerPath = folderName.replace("\\", "/");

            // ✅ Write code file
            String fileName = getFileName(language);
            Files.writeString(Path.of(folderName, fileName), code);

            ProcessBuilder pb;

            switch (language) {

                case "python":
                    pb = new ProcessBuilder(
                            DOCKER, "run", "--rm", "-i",
                            "-v", dockerPath + ":/app",
                            "python:3.9",
                            "python", "/app/script.py"
                    );
                    break;

                case "java":
                    pb = new ProcessBuilder(
                            DOCKER, "run", "--rm", "-i",
                            "-v", dockerPath + ":/app",
                            "eclipse-temurin:17",
                            "sh", "-c",
                            "javac /app/Main.java && java -cp /app Main"
                    );
                    break;

                case "c":
                    pb = new ProcessBuilder(
                            DOCKER, "run", "--rm", "-i",
                            "-v", dockerPath + ":/app",
                            "gcc:latest",
                            "sh", "-c",
                            "gcc /app/main.c -o /app/main && /app/main"
                    );
                    break;

                case "cpp":
                    pb = new ProcessBuilder(
                            DOCKER, "run", "--rm", "-i",
                            "-v", dockerPath + ":/app",
                            "gcc:latest",
                            "sh", "-c",
                            "g++ /app/main.cpp -o /app/main && /app/main"
                    );
                    break;

                case "js":
                    pb = new ProcessBuilder(
                            DOCKER, "run", "--rm", "-i",
                            "-v", dockerPath + ":/app",
                            "node:18",
                            "node", "/app/script.js"
                    );
                    break;

                default:
                    return "Unsupported language";
            }

            Process process = pb.start();

            // ✅ SEND INPUT
            if (input != null && !input.isEmpty()) {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream())
                );
                writer.write(input);
                writer.flush();
                writer.close();
            }

            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
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

            deleteFolder(new File(folderName));

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

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }
}
