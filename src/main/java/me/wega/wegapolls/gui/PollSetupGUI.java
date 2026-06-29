package me.wega.wegapolls.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import me.wega.wegapolls.GuiUtil;
import me.wega.wegapolls.ItemBuilder;
import me.wega.wegapolls.TimeString;
import me.wega.wegapolls.WegaPolls;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollPage;
import me.wega.wegapolls.poll.PollQuestionMode;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;

import static me.wega.wegapolls.Msgs.msg;
import static me.wega.wegapolls.Scheduler.runSync;

public class PollSetupGUI extends ChestGui {
    private final WegaPolls plugin;
    private final PollDefinition poll;

    public PollSetupGUI(WegaPolls plugin, PollDefinition poll) {
        super(6, poll.getPollName() + " Setup");
        this.plugin = plugin;
        this.poll = poll;

        final PaginatedPane questionPane = new PaginatedPane(9, 5);
        final StaticPane buttonPane = new StaticPane(9, 1);

        List<GuiItem> questionItems = new ArrayList<>();
        for (int i = 0; i < poll.size(); i++) {
            final int idx = i;
            final PollPage page = poll.getPage(i);

            List<Component> lore = new ArrayList<>();
            lore.add(msg("<white>Question #" + (i + 1)));
            lore.add(Component.empty());
            lore.add(msg("<white>Answers:"));
            page.getOptions().forEach(a -> lore.add(msg("<gray>- " + a)));
            lore.add(Component.empty());
            lore.add(msg("<white>Mode: <yellow>" + page.getMode().getLabel()));
            lore.add(msg("<gray>Click to edit this question"));

            questionItems.add(new GuiItem(
                    ItemBuilder.item(Material.GOLD_BLOCK)
                            .name(msg("<yellow>" + page.getQuestion()))
                            .setLore(lore)
                            .build(),
                    event -> {
                        event.setCancelled(true);
                        runSync(() ->
                                new PollQuestionSetupGUI(plugin, poll, idx).show(event.getWhoClicked())
                        );
                    }
            ));
        }
        questionPane.populateWithGuiItems(questionItems);

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.NAME_TAG)
                                .name(msg("<yellow>Set Poll Name"))
                                .setLore(msg("<gray>Click to set the poll name"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            HumanEntity clicker = event.getWhoClicked();
                            runSync(() -> {
                                clicker.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                clicker.sendMessage(msg("<yellow>Please enter the new poll name in chat. Type 'cancel' to cancel."));
                            });
                            plugin.getChatInputService().await(clicker,
                                    name -> {
                                        poll.setPollName(name);
                                        runSync(() ->
                                                new PollSetupGUI(plugin, poll).show(clicker)
                                        );
                                    },
                                    () -> {
                                        clicker.sendMessage(msg("<red>Poll name input cancelled."));
                                        runSync(() ->
                                                new PollSetupGUI(plugin, poll).show(clicker)
                                        );
                                    }
                            );
                        }
                ),
                Slot.fromIndex(0)
        );

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.CLOCK)
                                .name(msg("<yellow>Set Poll Duration"))
                                .setLore(msg("<gray>Click to add time to the poll end time"), msg("<gray>Example: 1d30min30sec"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            HumanEntity clicker = event.getWhoClicked();
                            runSync(() -> {
                                clicker.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                clicker.sendMessage(msg("<yellow>Please enter the duration to add. Type 'cancel' to cancel."));
                                clicker.sendMessage(msg("<gray>Example: 1d30min30sec"));
                            });
                            this.awaitDuration(clicker);
                        }
                ),
                Slot.fromIndex(2)
        );

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.GREEN_WOOL)
                                .name(msg("<green>Add Question"))
                                .setLore(msg("<gray>Click to add a new question"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            poll.addPage(
                                    PollPage.builder()
                                            .question("New Question")
                                            .mode(PollQuestionMode.SINGLE_CHOICE).options(List.of("Answer 1", "Answer 2"))
                                            .build()
                            );
                            runSync(() ->
                                    new PollQuestionSetupGUI(plugin, poll, poll.size() - 1).show(event.getWhoClicked()));
                        }
                ),
                Slot.fromIndex(4)
        );

        GuiUtil.addPaginationButtons(this, buttonPane, questionPane, 6, 7, 8, "Question browser");

        this.addPane(Slot.fromIndex(0), questionPane);
        this.addPane(Slot.fromXY(0, 5), buttonPane);
        this.setOnGlobalClick(e -> e.setCancelled(true));
    }

    private void awaitDuration(HumanEntity player) {
        plugin.getChatInputService().await(player,
                input -> {
                    try {
                        poll.setEndTime(poll.getEndTime().plusMillis(new TimeString(input).getTimeMillis()));
                        runSync(() ->
                                new PollSetupGUI(plugin, poll).show(player)
                        );
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(msg("<red>Invalid duration input."));
                        player.sendMessage(msg("<gray>Example: 1d30min30sec"));
                        this.awaitDuration(player);
                    }
                },
                () -> {
                    player.sendMessage(msg("<red>Poll duration input cancelled."));
                    runSync(() ->
                            new PollSetupGUI(plugin, poll).show(player)
                    );
                }
        );
    }
}