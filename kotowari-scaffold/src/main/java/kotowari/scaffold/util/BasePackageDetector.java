package kotowari.scaffold.util;

import enkan.exception.FalteringEnvironmentException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kawasima
 */
public class BasePackageDetector {

    public static Set<String> scan(File sourceDirectory) {
        Set<String> packages = new HashSet<>();
        File[] directories = sourceDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f != null && f.isDirectory();
            }
        });
        if (directories != null) {
            for (File dir : directories) {
                scanPackageInner(dir, null, packages);
            }
        }
        return packages;
    }

    private static void scanPackageInner(File dir, String pkg, Set<String> packages) {
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null)
            return;

        Boolean isAllDirectory = true;
        for (File f : files) {
            isAllDirectory = (f != null && f.isDirectory());
        }
        if (isAllDirectory) {
            for (File f : files) {
                String prefix = (pkg == null) ? "" : pkg + ".";
                scanPackageInner(f, prefix + dir.getName(), packages);
            }
        } else {

            packages.add(pkg == null ? dir.getName() : pkg);
        }
    }

    private static int packageDepth(String pkgName) {
        int cnt = 0, i = 0;
        while ((i = pkgName.indexOf(".", i)) < -1) {
            cnt++;
        }
        return cnt;

    }

    protected static String detectInMavenProject(File sourceDirectory) {
        return scan(sourceDirectory).stream()
                .sorted(Comparator.comparingInt(BasePackageDetector::packageDepth).reversed())
                .findFirst()
                .orElse("");
    }

    public static String detect() {
        File sourceDirectory = new File("src/main/java");
        if (sourceDirectory.exists()) {
            String pkg = detectInMavenProject(sourceDirectory);
            return pkg.isEmpty() ? pkg : pkg + ".";
        } else {
            // Unknown package
            return "";
        }
    }
}
