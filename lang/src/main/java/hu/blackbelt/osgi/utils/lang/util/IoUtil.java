package hu.blackbelt.osgi.utils.lang.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public final class IoUtil {
    private IoUtil() {
    }
    
    public static void copyDirectory(File source, File destination) throws IOException {
        destination.mkdirs();
        Files.walkFileTree(source.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, 
                new CopyDirectoryVisitor(source.toPath(), destination.toPath()));
    }

    private static final class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {
        private final Path source;
        private final Path target;
 
        private CopyDirectoryVisitor(Path source, Path target) {
            this.source = source;
            this.target = target;
        }
 
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes sourceBasic) throws IOException {
            Files.createDirectories(target.resolve(source.relativize(dir)));
            return FileVisitResult.CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES,  StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }
}
