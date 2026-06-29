package me.wega.wegapolls.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import me.wega.wegapolls.WegaPolls;
import me.wega.wegapolls.gui.PollsAdminGUI;
import me.wega.wegapolls.poll.PollRegistry;

public final class AdminPollsCmd extends CommandAPICommand {
    public AdminPollsCmd(WegaPolls plugin, PollRegistry pollRegistry) {
        super("adminpolls");
        withPermission("wegapolls.polls")
                .executesPlayer((sender, _) -> {
                    new PollsAdminGUI(plugin, pollRegistry).show(sender);
                })
                .register();
    }
}