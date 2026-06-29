package me.wega.wegapolls.input;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.HumanEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static me.wega.wegapolls.Scheduler.runSync;

@RequiredArgsConstructor
public final class ChatInputService {
    private final Map<UUID, Callbacks> callbacks = new ConcurrentHashMap<>();

    public void await(HumanEntity player, Consumer<String> onInput, Runnable onCancel) {
        callbacks.put(player.getUniqueId(), new Callbacks(onInput, onCancel));
    }

    public void submit(HumanEntity player, String input) {
        Callbacks cb = callbacks.remove(player.getUniqueId());
        if (cb == null) return;
        runSync(() -> cb.onInput.accept(input));
    }

    public void cancel(HumanEntity player) {
        Callbacks cb = callbacks.remove(player.getUniqueId());
        if (cb != null) cb.onCancel.run();
    }

    public boolean isAwaiting(HumanEntity player) {
        return callbacks.containsKey(player.getUniqueId());
    }

    private record Callbacks(Consumer<String> onInput, Runnable onCancel) {
    }
}