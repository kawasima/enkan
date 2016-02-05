package enkan.system.devel;

import enkan.exception.FalteringEnvironmentException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author kawasima
 */
public class ClassWatcher implements Runnable {
    private WatchService watchService;
    private ConcurrentHashMap<WatchKey, Path> watchings = new ConcurrentHashMap<>();
    private Runnable callback;

    public ClassWatcher(Set<Path> directories, Runnable callback) throws IOException {
        this.callback = callback;
        watchService = FileSystems.getDefault().newWatchService();
        directories.forEach(this::registerAll);
    }

    private void registerAll(final Path base) {
        try {
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw FalteringEnvironmentException.create(ex);
        }
    }

    private void register(final Path dir) {
        try {
            if (dir == null) return;
            WatchKey key = dir.register(this.watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watchings.put(key, dir);
        } catch(IOException ex) {
            throw FalteringEnvironmentException.create(ex);
        }
    }

    @Override
    public void run() {
        for (;;) {
            WatchKey key;

            try {
                key = watchService.poll(3, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path path = watchings.get(key).resolve(pathEvent.context());

                if (kind == ENTRY_MODIFY) {
                    if (!path.toFile().isDirectory()) {
                        callback.run();
                    }
                }

                if (kind == ENTRY_CREATE) {

                    if (!path.toFile().isDirectory()) {
                        callback.run();
                    }
                    if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                        registerAll(path);
                    }
                }
            }

            if (!key.reset()) {
                watchings.remove(key);

                if (watchings.isEmpty()) break;
            }
        }

    }
}
