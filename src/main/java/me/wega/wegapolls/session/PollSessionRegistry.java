package me.wega.wegapolls.session;

import lombok.Getter;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.session.answer.PollAnswer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public final class PollSessionRegistry {
    private final Map<UUID, PollSession> activeSessions = new HashMap<>();
    private final Map<UUID, Set<String>> completedPolls = new HashMap<>();

    @Getter
    private final PollStatsRegistry statsRegistry;

    public PollSessionRegistry(PollStatsRegistry statsRegistry) {
        this.statsRegistry = statsRegistry;
    }
    
    @Unmodifiable
    public Map<UUID, PollSession> getActiveSessions() {
        return Map.copyOf(activeSessions);
    }
    
    @Unmodifiable
    public Map<UUID, Set<String>> getCompletedPolls() {
        return Map.copyOf(completedPolls);
    }
    
    public PollSession getOrCreateSession(Player player, PollDefinition poll) {
        PollSession current = activeSessions.get(player.getUniqueId());
        if (current != null && current.getPoll() == poll) return current;
        PollSession session = new PollSession(poll);
        activeSessions.put(player.getUniqueId(), session);
        return session;
    }

    public PollSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public void clearSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }

    public boolean hasCompleted(Player player, PollDefinition poll) {
        Set<String> done = completedPolls.get(player.getUniqueId());
        return done != null && done.contains(poll.getPollName().toLowerCase());
    }

    public void markCompleted(Player player, PollDefinition poll) {
        List<PollAnswer> answers = List.copyOf(getSession(player).getAnswers());
        statsRegistry.record(player.getUniqueId(), poll, answers);
        completedPolls.computeIfAbsent(player.getUniqueId(), _ -> new HashSet<>())
                .add(poll.getPollName().toLowerCase());
        clearSession(player);
    }
}