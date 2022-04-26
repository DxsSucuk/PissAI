package de.presti.webpanel;

import de.presti.webpanel.sql.entities.URLEntry;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebpanelApplication {

    @Bean
    BeforeConvertCallback<URLEntry> idGeneratingCallback(DatabaseClient databaseClient) {

        return (entry, sqlIdentifier) -> {

            if (entry.getId() == null) {

                return databaseClient.sql("SELECT primary_key.nextval") //
                        .map(row -> row.get(0, Long.class)) //
                        .first() //
                        .map(entry::withId);
            }

            return Mono.just(entry);
        };
    }

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

        var initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ByteArrayResource((
                "DROP TABLE IF EXISTS urlentries;" +
                "CREATE TABLE urlentries (id BIGINT PRIMARY KEY, url VARCHAR(100) NOT NULL);")
                .getBytes())));

        return initializer;
    }

    public static void main(String[] args) {
        SpringApplication.run(WebpanelApplication.class, args);
    }

}
