package me.wega.wegapolls.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import me.wega.wegapolls.GuiUtil;
import me.wega.wegapolls.ItemBuilder;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollRegistry;
import me.wega.wegapolls.renderer.BookPollRenderer;
import me.wega.wegapolls.session.PollSession;
import me.wega.wegapolls.session.PollSessionRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static me.wega.wegapolls.Msgs.msg;
import static me.wega.wegapolls.Scheduler.runSync;

public class PlayerPollsGUI extends ChestGui {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public PlayerPollsGUI(PollRegistry pollRegistry, PollSessionRegistry sessionRegistry, Player viewer) {
        super(6, "Available Polls");

        final PaginatedPane pollPane = new PaginatedPane(9, 5);
        final StaticPane buttonPane = new StaticPane(9, 1);

        pollPane.populateWithGuiItems(pollRegistry.all().stream()
                .filter(PollDefinition::isAvailable) // only open + not expired
                .map(poll -> {
                    boolean done = sessionRegistry.hasCompleted(viewer, poll);
                    return new GuiItem(
                            ItemBuilder.item(done ? Material.LIME_CONCRETE : Material.BOOK)
                                    .name(msg("<yellow>" + poll.getPollName()))
                                    .setLore(
                                            msg("<white>Questions: <yellow>" + poll.getPages().size()),
                                            msg("<white>Ends: <yellow>" + TIME_FMT.format(LocalDateTime.ofInstant(poll.getEndTime(), ZoneId.systemDefault()))),
                                            Component.empty(),
                                            done ? msg("<green>✔ Already completed") : msg("<gray>Click to start")
                                    )
                                    .build(),
                            event -> {
                                event.setCancelled(true);
                                if (!poll.isAvailable()) {
                                    viewer.sendMessage(msg("<red>This poll is no longer available."));
                                    return;
                                }
                                if (sessionRegistry.hasCompleted(viewer, poll)) {
                                    viewer.sendMessage(msg("<red>You have already completed this poll."));
                                    return;
                                }
                                PollSession session = sessionRegistry.getOrCreateSession(viewer, poll);
                                runSync(() ->
                                        new BookPollRenderer(sessionRegistry).render(viewer, session)
                                );
                            }
                    );
                })
                .collect(Collectors.toList())
        );

        GuiUtil.addPaginationButtons(this, buttonPane, pollPane, 0, 4, 8, "Poll browser");
        this.addPane(Slot.fromIndex(0), pollPane);
        this.addPane(Slot.fromXY(0, 5), buttonPane);
        this.setOnGlobalClick(e -> e.setCancelled(true));
    }
}