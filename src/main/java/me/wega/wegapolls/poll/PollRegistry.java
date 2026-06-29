package me.wega.wegapolls.poll;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public final class PollRegistry {
    private final List<PollDefinition> polls = new ArrayList<>();

    public boolean register(PollDefinition poll) {
        if (find(poll.getPollName()) != null) return false;
        polls.add(poll);
        return true;
    }

    public boolean unregister(String pollName) {
        return polls.removeIf(p -> p.getPollName().equalsIgnoreCase(pollName));
    }

    public @Nullable PollDefinition find(String pollName) {
        return polls.stream()
                .filter(p -> p.getPollName().equalsIgnoreCase(pollName))
                .findFirst().orElse(null);
    }

    public @Unmodifiable List<PollDefinition> all() {
        return List.copyOf(polls);
    }
}