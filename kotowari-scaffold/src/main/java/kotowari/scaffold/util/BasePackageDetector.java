package kotowari.scaffold.util;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kawasima
 */
public class BasePackageDetector {

    public static Set<String> scan(File sourceDirectory) {
        Set<String> packages = new HashSet<>();
        File[] directories = sourceDirectory.listFiles(f -> f != null && f.isDirectory());
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

        boolean isAllDirectory = true;
        for (File f : files) {
            isAllDirectory &= (f != null && (f.isDirectory() || f.isHidden()));
        }
        if (isAllDirectory) {
            for (File f : files) {
                String prefix = (pkg == null) ? "" : pkg + ".";
                scanPackageInner(f, prefix + dir.getName(), packages);
            }
        } else {

            packages.add(pkg == null ? dir.getName() : pkg + "." + dir.getName());
        }
    }

    private static int packageDepth(String pkgName) {
        int cnt = 0, i = 0;
        while ((i = pkgName.indexOf(".", i)) == -1) {
            cnt++;
        }
        return cnt;

    }

    protected static String detectInMavenProject(File sourceDirectory) {
        return scan(sourceDirectory).stream()
                .filter(pkg -> !pkg.equals("db.migration"))
                .max(Comparator.comparing(BasePackageDetector::packageDepth))
                .orElse("");
    }

    public static String detect(String sourcePath) {
        File sourceDirectory = new File(sourcePath);
        if (sourceDirectory.exists()) {
            String pkg = detectInMavenProject(sourceDirectory);
            return pkg.isEmpty() ? pkg : pkg + ".";
        } else {
            // Unknown package
            return "";
        }
    }

    public static String detect() {
        return detect("src/main/java");
    }
}
