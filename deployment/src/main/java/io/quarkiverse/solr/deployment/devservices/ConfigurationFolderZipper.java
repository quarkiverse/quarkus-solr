package io.quarkiverse.solr.deployment.devservices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jspecify.annotations.NonNull;

public class ConfigurationFolderZipper {
    private static final String CONFIG_PROPERTY = "quarkus.solr.devservices.configuration";
    private static final String ERROR_MSG = "Could not read configuration directory '%s'. Please make sure %s points to a valid folder";

    private ConfigurationFolderZipper() {
    }

    public static byte[] zipFolder(String dirName) {
        Path folder = getPath(dirName);
        try (ZippingFileVisitor visitor = new ZippingFileVisitor(folder)) {
            Files.walkFileTree(folder, visitor);
            return visitor.getResult();
        } catch (Exception e) {
            throw new RuntimeException(ERROR_MSG.formatted(dirName, CONFIG_PROPERTY), e);
        }
    }

    private static Path getPath(String dirName) {
        File file;
        URL url = ConfigurationFolderZipper.class.getResource(dirName);
        if (url != null) {
            String protocol = url.getProtocol();
            if (!"file".equals(protocol)) {
                throw new RuntimeException(ERROR_MSG.formatted(dirName, CONFIG_PROPERTY));
            }
            file = new File(url.getPath());
        } else {
            file = new File(dirName);
        }
        if (!file.isDirectory()) {
            throw new RuntimeException(ERROR_MSG.formatted(dirName, CONFIG_PROPERTY));
        }
        return file.toPath();
    }

    private static class ZippingFileVisitor extends SimpleFileVisitor<Path> implements AutoCloseable {
        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        private final ZipOutputStream zos = new ZipOutputStream(bos);
        private final Path sourceDir;

        ZippingFileVisitor(Path sourceDir) {
            this.sourceDir = sourceDir;
        }

        @Override
        public void close() throws Exception {
            zos.close();
            bos.close();
        }

        public byte[] getResult() {
            return bos.toByteArray();
        }

        @Override
        public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
            String relativePath = sourceDir.relativize(file).toString().replace("\\", "/");
            zos.putNextEntry(new ZipEntry(relativePath));
            Files.copy(file, zos);
            zos.closeEntry();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public @NonNull FileVisitResult preVisitDirectory(@NonNull Path dir, @NonNull BasicFileAttributes attrs)
                throws IOException {
            String relativePath = sourceDir.relativize(dir).toString().replace("\\", "/");
            if (!relativePath.isEmpty()) {
                zos.putNextEntry(new ZipEntry(relativePath));
                zos.closeEntry();
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
