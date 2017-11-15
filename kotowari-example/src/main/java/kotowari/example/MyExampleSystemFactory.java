package kotowari.example;

import enkan.Env;
import enkan.collection.OptionMap;
import enkan.component.*;
import enkan.component.builtin.HmacEncoder;
import enkan.component.doma2.DomaProvider;
import enkan.component.flyway.FlywayMigration;
import enkan.component.freemarker.FreemarkerTemplateEngine;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.component.jackson.JacksonBeansConverter;
import enkan.component.metrics.MetricsComponent;
import enkan.component.undertow.UndertowComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

/**
 * @author kawasima
 */
public class MyExampleSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
                "hmac", new HmacEncoder(),
                "doma", new DomaProvider(),
                "jackson", new JacksonBeansConverter(),
                "flyway", new FlywayMigration(),
                "template", new FreemarkerTemplateEngine(),
                "metrics", new MetricsComponent(),
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test")),
                "app", new ApplicationComponent("kotowari.example.MyApplicationFactory"),
                "http", builder(new UndertowComponent())
                        .set(WebServerComponent::setPort, Env.getInt("PORT", 3000))
                        .set(WebServerComponent::setSslPort, Env.getInt("SSL_PORT", 3002))
                        .set(WebServerComponent::setSsl, Env.getInt("SSL_PORT", 0) != 0)
                        .set(WebServerComponent::setKeystorePath, Env.get("KEYSTORE_PATH"))
                        .set(WebServerComponent::setKeystorePassword, Env.get("KEYSTORE_PASSWORD"))
                        .build()
        ).relationships(
                component("http").using("app"),
                component("app").using("datasource", "template", "doma", "jackson", "metrics", "hmac"),
                component("doma").using("datasource", "flyway"),
                component("flyway").using("datasource")
        );

    }

}
