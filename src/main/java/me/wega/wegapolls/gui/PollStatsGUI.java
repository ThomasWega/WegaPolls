package me.wega.wegapolls.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import me.wega.wegapolls.ItemBuilder;
import me.wega.wegapolls.WegaPolls;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollPage;
import me.wega.wegapolls.poll.PollRegistry;
import me.wega.wegapolls.session.PollStatsRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static me.wega.wegapolls.Msgs.msg;
import static me.wega.wegapolls.Scheduler.runSync;

public class PollStatsGUI extends ChestGui {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public PollStatsGUI(WegaPolls plugin, PollRegistry pollRegistry, PollDefinition poll) {
        super(6, poll.getPollName() + " — Stats");

        PollStatsRegistry stats = plugin.getPollStatsRegistry();
        int totalRespondents = stats.getRespondentCount(poll);

        final PaginatedPane questionPane = new PaginatedPane(9, 5);
        final StaticPane buttonPane = new StaticPane(9, 1);

        List<GuiItem> items = new ArrayList<>();
        for (int qi = 0; qi < poll.size(); qi++) {
            PollPage page = poll.getPage(qi);

            List<Component> lore = new ArrayList<>();
            lore.add(msg("<gray>Mode: <yellow>" + page.getMode().getLabel()));
            lore.add(msg("<gray>Respondents: <white>" + totalRespondents));
            lore.add(Component.empty());

            int maxVotes = 0;
            int[] votes = new int[page.getOptions().size()];
            for (int oi = 0; oi < page.getOptions().size(); oi++) {
                votes[oi] = stats.getVoteCount(poll, qi, oi);
                if (votes[oi] > maxVotes) maxVotes = votes[oi];
            }

            for (int oi = 0; oi < page.getOptions().size(); oi++) {
                String option = page.getOptions().get(oi);
                int v = votes[oi];
                double pct = totalRespondents == 0 ? 0.0 : (v * 100.0 / totalRespondents);
                String bar = buildBar(v, maxVotes, 10);
                lore.add(msg("<white>" + (oi + 1) + ". " + option));
                lore.add(msg("<green>" + bar + " <yellow>" + v + " <gray>(" + String.format("%.1f", pct) + "%)"));
                lore.add(Component.empty());
            }

            items.add(new GuiItem(
                    ItemBuilder.item(Material.PAPER)
                            .name(msg("<yellow>Q" + (qi + 1) + ": <white>" + page.getQuestion()))
                            .setLore(lore)
                            .build(),
                    event -> event.setCancelled(true)
            ));
        }
        questionPane.populateWithGuiItems(items);
        
        // Back button
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.ARROW)
                                .name(msg("<yellow>Back"))
                                .setLore(msg("<gray>Return to polls list"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            runSync(() ->
                                    new PollsAdminGUI(plugin, pollRegistry).show(event.getWhoClicked()));
                        }
                ),
                Slot.fromIndex(0)
        );

        // Overview item
        boolean expired = !poll.getEndTime().isAfter(Instant.now());
        String statusStr = expired ? "<red>Expired" : (poll.isOpen() ? "<green>Open" : "<yellow>Closed");
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.COMPARATOR)
                                .name(msg("<aqua>Poll Overview"))
                                .setLore(
                                        msg("<white>Name: <yellow>" + poll.getPollName()),
                                        msg("<white>Status: " + statusStr),
                                        msg("<white>End time: <gray>" + TIME_FMT.format(LocalDateTime.ofInstant(poll.getEndTime(), ZoneId.systemDefault()))),
                                        msg("<white>Questions: <gray>" + poll.size()),
                                        Component.empty(),
                                        msg("<white>Total respondents: <green>" + totalRespondents)
                                )
                                .build(),
                        event -> event.setCancelled(true)
                ),
                Slot.fromIndex(4)
        );

        // Pagination
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.ARROW)
                                .name(msg("<yellow>Previous Page"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            if (questionPane.getPage() > 0) {
                                questionPane.setPage(questionPane.getPage() - 1);
                                this.update();
                            }
                        }
                ),
                Slot.fromIndex(6)
        );

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.PAPER)
                                .name(msg("<yellow>Page " + (questionPane.getPage() + 1)))
                                .build(),
                        event -> event.setCancelled(true)
                ),
                Slot.fromIndex(7)
        );

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.ARROW)
                                .name(msg("<yellow>Next Page"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            if (questionPane.getPage() + 1 < questionPane.getPages()) {
                                questionPane.setPage(questionPane.getPage() + 1);
                                this.update();
                            }
                        }
                ),
                Slot.fromIndex(8)
        );

        this.addPane(Slot.fromIndex(0), questionPane);
        this.addPane(Slot.fromXY(0, 5), buttonPane);
        this.setOnGlobalClick(e -> e.setCancelled(true));
    }

    /**
     * Builds a simple Unicode block bar proportional to votes/maxVotes.
     * e.g. "██████░░░░" for 6/10 votes
     */
    private static String buildBar(int votes, int max, int width) {
        if (max == 0)
            return "<dark_gray>" + "░".repeat(width);
        int filled = (int) Math.round((double) votes / max * width);
        return "<green>" + "█".repeat(filled) + "<dark_gray>" + "░".repeat(width - filled);
    }
}