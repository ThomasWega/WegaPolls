package me.wega.wegapolls.renderer;

import me.wega.wegapolls.session.PollSession;
import org.bukkit.entity.Player;

public interface PollRenderer {
    void render(Player player, PollSession session);
}