package de.presti.webpanel.sql.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Data
@RequiredArgsConstructor
@Table("urlentries")
public class URLEntry {

    @Id
    @NonNull
    private Long id;

    @NonNull
    private String url;

    public URLEntry withId(Long id) {
        return new URLEntry(id, "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof URLEntry entry)) {
            return false;
        }
        return Objects.equals(getUrl(), entry.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl());
    }
}
