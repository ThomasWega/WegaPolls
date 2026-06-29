package me.wega.wegapolls.session;

import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.session.answer.ChoicePollAnswer;
import me.wega.wegapolls.session.answer.PollAnswer;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public final class PollStatsRegistry {

    private final Map<String, PollStats> statsByPoll = new HashMap<>();

    @Unmodifiable
    public List<PollStats> getAll() {
        return List.copyOf(statsByPoll.values());
    }

    public void load(PollStats stats) {
        statsByPoll.put(stats.getPollName(), stats);
    }

    public void record(UUID player, PollDefinition poll, List<PollAnswer> answers) {
        PollStats pollStats = statsByPoll.computeIfAbsent(
            poll.getPollName().toLowerCase(), PollStats::new);
        pollStats.getRespondents().add(player);

        for (int i = 0; i < answers.size(); i++) {
            if (!(answers.get(i) instanceof ChoicePollAnswer(Set<Integer> selectedOptions))) continue;
            pollStats.recordAnswer(player, i, selectedOptions);
        }
    }

    public int getRespondentCount(PollDefinition poll) {
        PollStats s = statsByPoll.get(poll.getPollName().toLowerCase());
        return s == null ? 0 : s.getRespondents().size();
    }

    public int getVoteCount(PollDefinition poll, int questionIndex, int optionIndex) {
        PollStats s = statsByPoll.get(poll.getPollName().toLowerCase());
        return s == null ? 0 : s.getVoteCount(questionIndex, optionIndex);
    }
}