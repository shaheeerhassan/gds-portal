package com.school.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnvLoader {

    private static final Map<String, String> FILE_VALUES = new HashMap<>();

    static {
        load();
    }

    private EnvLoader() {
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv().get(key);
        if (envValue != null && !envValue.isBlank())
            return envValue;
        synchronized (FILE_VALUES) {
            String fileValue = FILE_VALUES.get(key);
            if (fileValue != null)
                return fileValue;
        }
        return defaultValue;
    }

    private static void load() {
        Path path = findEnvFile();
        if (path == null)
            return;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#"))
                    continue;
                int idx = trimmed.indexOf('=');
                if (idx <= 0)
                    continue;
                String key = trimmed.substring(0, idx).trim();
                if (key.isEmpty())
                    continue;
                String value = trimmed.substring(idx + 1).trim();
                value = stripQuotes(value);
                FILE_VALUES.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read .env file: " + e.getMessage());
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\''))
                return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static Path findEnvFile() {
        String custom = System.getProperty("env.file");
        if (custom != null && !custom.isBlank()) {
            Path customPath = Paths.get(custom);
            if (Files.isReadable(customPath))
                return customPath;
        }

        String userDir = System.getProperty("user.dir");
        String[] candidates = {
            userDir + "/.env",
            userDir + "/../.env"
        };
        String catalinaBase = System.getenv("CATALINA_BASE");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            String[] tomcatCandidates = {
                catalinaBase + "/.env",
                catalinaBase + "/../.env"
            };
            String[] combined = new String[candidates.length + tomcatCandidates.length];
            System.arraycopy(candidates, 0, combined, 0, candidates.length);
            System.arraycopy(tomcatCandidates, 0, combined, candidates.length, tomcatCandidates.length);
            candidates = combined;
        }

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (Files.isReadable(path))
                return path;
        }
        return null;
    }
}
