package de.presti.webpanel.sql.repository;

import de.presti.webpanel.sql.entities.URLEntry;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class URLEntryService {

    @Autowired
    private URLRepository repository;

    public Boolean isValid(final URLEntry urlEntry) {
        if (urlEntry != null && !urlEntry.getUrl().isEmpty()) {
            return true;
        }

        return false;
    }
    public Flux getAllTasks() {
        return this.repository.findAll();
    }
    public Mono createTask(final URLEntry urlEntry) {
        return this.repository.save(urlEntry);
    }

    @Transactional
    public Mono updateTask(final URLEntry urlEntry) {
        return this.repository.findById(urlEntry.getId())
                .flatMap(t -> {
                    t.setUrl(urlEntry.getUrl());
                    return this.repository.save(t);
                });
    }

    @Transactional
    public Mono deleteTask(final long id){
        return this.repository.findById(id)
                .flatMap(this.repository::delete);
    }
}
