package me.wega.wegapolls.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import me.wega.wegapolls.GuiUtil;
import me.wega.wegapolls.ItemBuilder;
import me.wega.wegapolls.WegaPolls;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollPage;
import me.wega.wegapolls.poll.PollQuestionMode;
import me.wega.wegapolls.poll.PollRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.wega.wegapolls.Msgs.msg;
import static me.wega.wegapolls.Scheduler.runSync;

public class PollsAdminGUI extends ChestGui {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public PollsAdminGUI(WegaPolls plugin, PollRegistry pollRegistry) {
        super(6, "Admin Polls");

        final PaginatedPane pollPane = new PaginatedPane(9, 5);
        final StaticPane buttonPane = new StaticPane(9, 1);

        pollPane.populateWithGuiItems(pollRegistry.all().stream()
                .map(poll -> {
                    boolean open = poll.isOpen();
                    boolean expired = !poll.getEndTime().isAfter(Instant.now());
                    Material mat = expired ? Material.RED_CONCRETE : (open ? Material.LIME_CONCRETE : Material.YELLOW_CONCRETE);
                    String statusTag = expired ? "<red>[EXPIRED]" : (open ? "<green>[OPEN]" : "<yellow>[CLOSED]");

                    return new GuiItem(
                            ItemBuilder.item(mat)
                                    .name(msg(statusTag + " <white>" + poll.getPollName()))
                                    .setLore(
                                            msg("<white>Questions: <gray>" + poll.getPages().size()),
                                            msg("<white>Ends: <gray>" + TIME_FMT.format(LocalDateTime.ofInstant(poll.getEndTime(), ZoneId.systemDefault()))),
                                            msg("<white>Status: " + (expired ? "<red>Expired" : (open ? "<green>Open" : "<yellow>Closed"))),
                                            Component.empty(),
                                            msg("<yellow>Left-click <gray>to edit"),
                                            msg((open ? "<yellow>" : "<green>") + (open ? "Right-click <gray>to close" : "Right-click <gray>to open")),
                                            msg("<aqua>Drop-click <gray>to view stats"),
                                            msg("<red>Shift+right-click <gray>to delete")
                                    )
                                    .build(),
                            event -> {
                                event.setCancelled(true);

                                // Shift + right = delete
                                if (event.getClick().isShiftClick() && event.getClick().isRightClick()) {
                                    runSync(() -> {
                                        pollRegistry.unregister(poll.getPollName());
                                        new PollsAdminGUI(plugin, pollRegistry).show(event.getWhoClicked());
                                    });
                                    return;
                                }

                                // Middle click = stats
                                if (event.getClick() == ClickType.DROP) {
                                    runSync(() ->
                                            new PollStatsGUI(plugin, pollRegistry, poll).show(event.getWhoClicked())
                                    );
                                    return;
                                }

                                // Right click = open/close toggle
                                if (event.getClick().isRightClick()) {
                                    poll.setOpen(!poll.isOpen());
                                    runSync(() ->
                                            new PollsAdminGUI(plugin, pollRegistry).show(event.getWhoClicked())
                                    );
                                    return;
                                }

                                // Left click = edit
                                runSync(() ->
                                        new PollSetupGUI(plugin, poll).show(event.getWhoClicked())
                                );
                            }
                    );
                })
                .collect(Collectors.toList())
        );

        // Create new poll
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.EMERALD_BLOCK)
                                .name(msg("<green>Create New Poll"))
                                .setLore(msg("<gray>Click to create a new poll"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            HumanEntity clicker = event.getWhoClicked();
                            runSync(() -> {
                                clicker.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                clicker.sendMessage(msg("<green>Enter the name of the new poll in chat, or 'cancel' to cancel:"));
                            });
                            plugin.getChatInputService().await(clicker,
                                    pollName -> {
                                        PollDefinition newPoll = PollDefinition.builder()
                                                .pollName(pollName)
                                                .endTime(Instant.now().plus(30, TimeUnit.DAYS.toChronoUnit()))
                                                .page(PollPage.builder()
                                                        .question("Question 1")
                                                        .options(List.of("Answer 1", "Answer 2", "Answer 3"))
                                                        .mode(PollQuestionMode.SINGLE_CHOICE)
                                                        .build()
                                                )
                                                .build();
                                        pollRegistry.register(newPoll);
                                        runSync(() ->
                                                new PollSetupGUI(plugin, newPoll).show(clicker)
                                        );
                                    },
                                    () -> {
                                        clicker.sendMessage(msg("<red>Poll creation cancelled."));
                                        runSync(() ->
                                                new PollsAdminGUI(plugin, pollRegistry).show(clicker)
                                        );
                                    });
                        }
                ),
                Slot.fromIndex(0)
        );

        GuiUtil.addPaginationButtons(this, buttonPane, pollPane, 6, 7, 8, "Poll browser");

        this.addPane(Slot.fromIndex(0), pollPane);
        this.addPane(Slot.fromXY(0, 5), buttonPane);
        this.setOnGlobalClick(e -> e.setCancelled(true));
    }
}