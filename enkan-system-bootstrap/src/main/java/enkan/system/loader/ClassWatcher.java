package enkan.system.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author kawasima
 */
public class ClassWatcher implements Runnable {

    private WatchService service;
    private Path watchPath;
    private Runnable callback;

    public ClassWatcher(File watchDir, Runnable callback) throws IOException {
        this.callback = callback;
        watchPath = watchDir.toPath();
        service = watchPath.getFileSystem().newWatchService();
        Files.walkFileTree(watchPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void run() {
        while (true) {
            WatchKey key;
            try {
                key = service.take();
            } catch(InterruptedException e) {
                return;
            }

            if (key.isValid()) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) continue;

                    Path path = watchPath.resolve(((WatchEvent<Path>)event).context());
                    if (Files.isDirectory(path, NOFOLLOW_LINKS)) {
                        if (event.kind() == ENTRY_CREATE) {

                        }
                    } else if (Files.isRegularFile(path, NOFOLLOW_LINKS)) {
                        if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                            callback.run();
                        }
                    }
                }
            }
        }
    }
}
