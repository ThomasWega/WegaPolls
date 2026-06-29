package me.wega.wegapolls.session;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity("poll_stats")
@Getter
@NoArgsConstructor
public class PollStats {

    @Id
    private String pollName; // e.g. "mypoll"

    private Set<UUID> respondents = new HashSet<>();
    private Map<Integer, Map<Integer, Set<UUID>>> votesByQuestion = new HashMap<>();

    public PollStats(String pollName) {
        this.pollName = pollName;
    }

    void recordAnswer(UUID player, int questionIndex, Set<Integer> selectedOptions) {
        Map<Integer, Set<UUID>> votersByOption = votesByQuestion
            .computeIfAbsent(questionIndex, _ -> new HashMap<>());
        for (int option : selectedOptions)
            votersByOption.computeIfAbsent(option, _ -> new HashSet<>()).add(player);
    }

    public int getVoteCount(int questionIndex, int optionIndex) {
        return votesByQuestion
            .getOrDefault(questionIndex, Map.of())
            .getOrDefault(optionIndex, Set.of())
            .size();
    }
}