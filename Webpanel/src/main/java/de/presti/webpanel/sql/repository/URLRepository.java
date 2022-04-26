package de.presti.webpanel.sql.repository;

import de.presti.webpanel.sql.entities.URLEntry;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface URLRepository extends ReactiveCrudRepository<URLEntry, Long> {

    @Query("select id, url from urlentries urle where urle.url = :url")
    Flux<URLEntry> findByURL(String url);
}
