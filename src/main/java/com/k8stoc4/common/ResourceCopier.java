package com.k8stoc4.common;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.stream.Stream;

public class ResourceCopier {
    private ResourceCopier() {}

    public static void copyResourceDirectory(final String resourceDir, final Path destination) throws IOException, URISyntaxException {
        final URL resourceUrl = ResourceCopier.class.getClassLoader().getResource(resourceDir);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource directory not found: " + resourceDir);
        }

        final URI uri = resourceUrl.toURI();

        // Handle both IDE (file://) and JAR (jar://) environments
        if (uri.getScheme().equals("jar")) {
            try (final FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                final Path source = fs.getPath("/" + resourceDir);
                copyDirectory(source, destination);
            }
        } else {
            final Path source = Paths.get(uri);
            copyDirectory(source, destination);
        }
    }

    private static void copyDirectory(final Path source, final Path destination) throws IOException {
        try (final Stream<Path> paths = Files.walk(source)) {
            for (final Path sourcePath : (Iterable<Path>) paths::iterator) {
                final Path relative = source.relativize(sourcePath);
                // .toString() handles cross-FileSystem resolution
                final Path target = destination.resolve(relative.toString());

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}