package me.wega.wegapolls.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import me.wega.wegapolls.ItemBuilder;
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
import java.util.function.Consumer;

import static me.wega.wegapolls.Msgs.msg;
import static me.wega.wegapolls.Scheduler.runSync;

public class PollQuestionSetupGUI extends ChestGui {
    private final WegaPolls plugin;
    private final PollDefinition poll;
    private final int pageIndex;

    public PollQuestionSetupGUI(WegaPolls plugin, PollDefinition poll, int pageIndex) {
        super(3, poll.getPollName() + " Question");
        this.plugin = plugin;
        this.poll = poll;
        this.pageIndex = pageIndex;

        PollPage page = poll.getPage(pageIndex);
        final StaticPane infoPane = new StaticPane(9, 1);
        final OutlinePane answersPane = new OutlinePane(9, 1);
        final StaticPane buttonPane = new StaticPane(9, 1);

        // Question text button
        infoPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.BOOK)
                                .name(msg("<yellow>Question Text"))
                                .setLore(msg("<white>Current: <gray>" + page.getQuestion()), Component.empty(), msg("<gray>Click to edit the question text"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            this.openChatThen(
                                    event.getWhoClicked(),
                                    msg("<yellow>Please enter the new question text in chat. Type 'cancel' to cancel."),
                                    q -> this.updatePage(poll.getPage(pageIndex).withQuestion(q)),
                                    msg("<red>Question text input cancelled."));
                        }
                ),
                Slot.fromIndex(1)
        );

        // Mode toggle button
        infoPane.addItem(new GuiItem(
                        ItemBuilder.item(page.getMode() == PollQuestionMode.SINGLE_CHOICE ? Material.LEVER : Material.REDSTONE)
                                .name(msg("<yellow>Question Mode"))
                                .setLore(
                                        msg("<white>Current: <yellow>" + page.getMode().getLabel()),
                                        Component.empty(),
                                        msg("<gray>Click to toggle mode")
                                )
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            PollPage cur = poll.getPage(pageIndex);
                            PollQuestionMode newMode = cur.getMode() == PollQuestionMode.SINGLE_CHOICE
                                    ? PollQuestionMode.MULTI_CHOICE : PollQuestionMode.SINGLE_CHOICE;
                            poll.setPage(pageIndex, cur.withMode(newMode));
                            runSync(() ->
                                    new PollQuestionSetupGUI(plugin, poll, pageIndex).show(event.getWhoClicked())
                            );
                        }
                ),
                Slot.fromIndex(4)
        );

        // Add answer button
        infoPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.GREEN_WOOL)
                                .name(msg("<green>Add Answer"))
                                .setLore(msg("<gray>Click to add a new answer"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            this.openChatThen(
                                    event.getWhoClicked(),
                                    msg("<yellow>Please enter the new answer in chat. Type 'cancel' to cancel."),
                                    a -> {
                                        List<String> opts = new ArrayList<>(poll.getPage(pageIndex).getOptions());
                                        opts.add(a);
                                        this.updatePage(poll.getPage(pageIndex).withOptions(opts));
                                    },
                                    msg("<red>Answer input cancelled.")
                            );
                        }
                ),
                Slot.fromIndex(7)
        );

        // Answer items
        for (int i = 0; i < page.getOptions().size(); i++) {
            final int fi = i;
            final String option = page.getOptions().get(i);
            answersPane.addItem(new GuiItem(
                    ItemBuilder.item(Material.PAPER)
                            .name(msg("<white>Answer #" + (i + 1)))
                            .setLore(msg("<gray>" + option), Component.empty(), msg("<gray>Click to edit this answer"), msg("<red>Shift-right click to remove this answer"))
                            .build(),
                    event -> {
                        event.setCancelled(true);
                        HumanEntity clicker = event.getWhoClicked();
                        if (event.getClick().isShiftClick() && event.getClick().isRightClick()) {
                            List<String> opts = new ArrayList<>(poll.getPage(pageIndex).getOptions());
                            if (opts.size() <= 1) {
                                clicker.sendMessage(msg("<red>A question must have at least one answer."));
                                return;
                            }
                            opts.remove(fi);
                            this.updatePage(poll.getPage(pageIndex).withOptions(opts));
                            runSync(() ->
                                    new PollQuestionSetupGUI(plugin, poll, pageIndex).show(clicker)
                            );
                            return;
                        }
                        openChatThen(clicker, msg("<yellow>Please enter the new answer text in chat. Type 'cancel' to cancel."),
                                a -> {
                                    List<String> opts = new ArrayList<>(poll.getPage(pageIndex).getOptions());
                                    opts.set(fi, a);
                                    this.updatePage(poll.getPage(pageIndex).withOptions(opts));
                                },
                                msg("<red>Answer edit cancelled."));
                    }
            ));
        }

        // Back button
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.ARROW)
                                .name(msg("<yellow>Back"))
                                .setLore(msg("<gray>Return to poll setup"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            runSync(() ->
                                    new PollSetupGUI(plugin, poll).show(event.getWhoClicked())
                            );
                        }
                ),
                Slot.fromIndex(0)
        );

        // Delete question button
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.TNT)
                                .name(msg("<red>Delete Question"))
                                .setLore(msg("<gray>Removes this question from the poll"), msg("<red>This cannot be undone"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            poll.removePage(pageIndex);
                            if (poll.size() == 0)
                                poll.addPage(
                                        PollPage.builder()
                                                .question("New Question").mode(PollQuestionMode.SINGLE_CHOICE)
                                                .options(new ArrayList<>(List.of("Answer 1", "Answer 2")))
                                                .build()
                                );
                            runSync(() ->
                                    new PollSetupGUI(plugin, poll).show(event.getWhoClicked())
                            );
                        }
                ),
                Slot.fromIndex(8)
        );

        this.addPane(Slot.fromIndex(0), infoPane);
        this.addPane(Slot.fromXY(0, 1), answersPane);
        this.addPane(Slot.fromXY(0, 2), buttonPane);
        this.setOnGlobalClick(e -> e.setCancelled(true));
    }

    private void updatePage(PollPage newPage) {
        poll.setPage(pageIndex, newPage);
    }

    private void openChatThen(HumanEntity player, Component prompt,
                              Consumer<String> onInput, Component cancelMsg) {
        runSync(() -> {
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            player.sendMessage(prompt);
        });
        plugin.getChatInputService().await(player,
                input -> {
                    onInput.accept(input);
                    runSync(() ->
                            new PollQuestionSetupGUI(plugin, poll, pageIndex).show(player)
                    );
                },
                () -> {
                    player.sendMessage(cancelMsg);
                    runSync(() ->
                            new PollQuestionSetupGUI(plugin, poll, pageIndex).show(player)
                    );
                }
        );
    }
}