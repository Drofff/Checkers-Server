package com.drofff.checkers.server.utils;

import com.drofff.checkers.server.exception.CheckersServerException;
import com.drofff.checkers.server.exception.ValidationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileUtils {

    private FileUtils() {}

    public static String readFileFromClasspathAsStr(String uri) {
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        URL fileUrl = classLoader.getResource(uri);
        return Optional.ofNullable(fileUrl)
                .map(FileUtils::readFileAtURLAsStr)
                .orElseThrow(() -> new ValidationException("Classpath file at " + uri + " is not found"));
    }

    private static String readFileAtURLAsStr(URL url) {
        try {
            return readLinesOfFileAtURL(url);
        } catch(URISyntaxException | IOException e) {
            throw new CheckersServerException(e.getMessage());
        }
    }

    private static String readLinesOfFileAtURL(URL url) throws IOException, URISyntaxException {
        Path filePath = Paths.get(url.toURI());
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

}