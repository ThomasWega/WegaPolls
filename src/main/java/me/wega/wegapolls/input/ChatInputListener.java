package me.wega.wegapolls.input;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.wega.wegapolls.Msgs.msg;

@RequiredArgsConstructor
public final class ChatInputListener implements Listener {
    private final ChatInputService chatInputService;

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var player = event.getPlayer();
        if (!chatInputService.isAwaiting(player)) return;

        event.setCancelled(true);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (input.equalsIgnoreCase("cancel")) {
            chatInputService.cancel(player);
            player.sendMessage(msg("<red>Input cancelled.</red>"));
            return;
        }
        chatInputService.submit(player, input);
    }
}