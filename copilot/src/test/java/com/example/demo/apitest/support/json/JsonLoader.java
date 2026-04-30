package com.example.demo.apitest.support.json;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility for loading JSON test fixtures from classpath resources.
 * Supports template variables using ${variable} syntax.
 */
@UtilityClass
public class JsonLoader {

    /**
     * Load JSON content from classpath resource.
     */
    public String load(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource("test-data/" + resourcePath);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load test data: " + resourcePath, e);
        }
    }

    /**
     * Load JSON content and replace template variables.
     * Variables use ${variableName} syntax.
     */
    public String load(String resourcePath, Map<String, String> variables) {
        String content = load(resourcePath);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return content;
    }
}
