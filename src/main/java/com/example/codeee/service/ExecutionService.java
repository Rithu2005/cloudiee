package com.example.codeee.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

@Service
public class ExecutionService {

    public String runCode(String language, String code, String input) {

        try {
            String folder = "/app/code";   // ✅ shared folder
            Files.createDirectories(Path.of(folder));

            String filePath = "";
            String command = "";

            switch (language.toLowerCase()) {

                // 🐍 PYTHON
                case "python":
                    filePath = folder + "/script.py";
                    Files.writeString(Path.of(filePath), code);

                    command = "docker run --rm -i -v " + folder + ":/app -w /app python:3.10-slim python script.py";
                    break;

                // ☕ JAVA
                case "java":
                    filePath = folder + "/Main.java";
                    Files.writeString(Path.of(filePath), code);

                    command = "docker run --rm -i -v " + folder + ":/app -w /app eclipse-temurin:17-jdk-alpine sh -c \"javac Main.java && java Main\"";
                    break;

                // 🔵 C
                case "c":
                    filePath = folder + "/main.c";
                    Files.writeString(Path.of(filePath), code);

                    command = "docker run --rm -i -v " + folder + ":/app -w /app alpine sh -c \"apk add --no-cache gcc && gcc main.c -o main && ./main\"";
                    break;

                // 🔷 C++
                case "cpp":
                    filePath = folder + "/main.cpp";
                    Files.writeString(Path.of(filePath), code);

                    command = "docker run --rm -i -v " + folder + ":/app -w /app alpine sh -c \"apk add --no-cache g++ && g++ main.cpp -o main && ./main\"";
                    break;

                // 🟨 JAVASCRIPT
                case "js":
                    filePath = folder + "/script.js";
                    Files.writeString(Path.of(filePath), code);

                    command = "docker run --rm -i -v " + folder + ":/app -w /app node:18-alpine node script.js";
                    break;

                default:
                    return "Unsupported language";
            }

            // ✅ RUN COMMAND
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // ✅ HANDLE INPUT
            if (input != null && !input.isEmpty()) {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream())
                );
                writer.write(input);
                writer.newLine();
                writer.flush();
                writer.close();
            }

            // ✅ READ OUTPUT
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

            return output.toString();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
