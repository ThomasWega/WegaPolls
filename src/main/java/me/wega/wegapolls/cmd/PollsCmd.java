package me.wega.wegapolls.cmd;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.StringArgument;
import me.wega.wegapolls.gui.PlayerPollsGUI;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollRegistry;
import me.wega.wegapolls.renderer.BookPollRenderer;
import me.wega.wegapolls.session.PollSession;
import me.wega.wegapolls.session.PollSessionRegistry;

import static me.wega.wegapolls.Msgs.msg;

public final class PollsCmd extends CommandTree {
    public PollsCmd(PollRegistry pollRegistry, PollSessionRegistry sessionRegistry) {
        super("polls");

        executesPlayer((sender, _) -> {
            new PlayerPollsGUI(pollRegistry, sessionRegistry, sender).show(sender);
        })
                .then(new StringArgument("poll")
                        .executesPlayer((sender, args) -> {
                            String pollName = (String) args.get("poll");
                            PollDefinition poll = pollRegistry.find(pollName);

                            if (poll == null) {
                                sender.sendMessage(msg("<red>That poll does not exist."));
                                return;
                            }
                            if (!poll.isAvailable()) {
                                sender.sendMessage(msg("<red>That poll is not currently open."));
                                return;
                            }
                            if (sessionRegistry.hasCompleted(sender, poll)) {
                                sender.sendMessage(msg("<red>You have already completed this poll."));
                                return;
                            }

                            PollSession session = sessionRegistry.getOrCreateSession(sender, poll);
                            new BookPollRenderer(sessionRegistry).render(sender, session);
                        }))
                .register();
    }
}