package me.wega.wegapolls.poll;

import dev.morphia.annotations.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)  // Morphia requires a no-args constructor for deserialization
@AllArgsConstructor
@Entity
public class PollPage {
    String question;
    PollQuestionMode mode;
    List<String> options;

    public PollPage withQuestion(String q) {
        return this.toBuilder().question(q).build();
    }

    public PollPage withMode(PollQuestionMode m) {
        return this.toBuilder().mode(m).build();
    }

    public PollPage withOptions(List<String> o) {
        return this.toBuilder().options(o).build();
    }
}