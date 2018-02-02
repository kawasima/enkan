package enkan.system.command;

import enkan.component.SystemComponent;
import enkan.component.WebServerComponent;
import enkan.system.EnkanSystem;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class StartCommand implements SystemCommand {
    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        system.start();
        transport.sendOut("Started server");
        if (args.length > 0) {
            if (Desktop.isDesktopSupported()) {
                Optional<WebServerComponent> webServerComponent = system.getAllComponents().stream()
                        .filter(WebServerComponent.class::isInstance)
                        .map(WebServerComponent.class::cast)
                        .findFirst();
                webServerComponent.ifPresent(web -> {
                    try {
                        Desktop.getDesktop().browse(URI.create("http://localhost:" + web.getPort() + "/" + args[0].replaceAll("^/", "")));
                    } catch (IOException ignore) {
                        // ignore
                    }
                });
            }
        }
        return true;
    }
}
