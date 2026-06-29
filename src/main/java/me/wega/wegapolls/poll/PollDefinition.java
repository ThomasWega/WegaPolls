package me.wega.wegapolls.poll;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity("polls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollDefinition {
    @Id
    private ObjectId id;
    private String pollName;
    private Instant endTime;
    private boolean open;
    private List<PollPage> pages;
    
    @Builder
    public PollDefinition(String pollName, Instant endTime, @Singular("page") List<PollPage> pages) {
        this.pollName = pollName;
        this.endTime = endTime;
        this.open = false;
        this.pages = pages != null ? new ArrayList<>(pages) : new ArrayList<>();
    }

    public PollPage getPage(int index) {
        return pages.get(index);
    }

    public void setPage(int index, PollPage page) {
        pages.set(index, page);
    }

    public void addPage(PollPage page) {
        pages.add(page);
    }

    public void removePage(int index) {
        pages.remove(index);
    }

    public int size() {
        return pages.size();
    }

    public boolean isAvailable() {
        return open && Instant.now().isBefore(endTime);
    }
}