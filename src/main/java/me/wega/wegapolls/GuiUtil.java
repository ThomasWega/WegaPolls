package me.wega.wegapolls;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;

import static me.wega.wegapolls.Msgs.msg;

@UtilityClass
public final class GuiUtil {
    
    public static void addPaginationButtons(ChestGui gui, StaticPane buttonPane,
                                            PaginatedPane pane,
                                            int prevSlot, int midSlot, int nextSlot,
                                            String browserLabel) {
        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.ARROW)
                                .name(msg("<yellow>Previous Page"))
                                .setLore(msg("<gray>Go to the previous page"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            if (pane.getPage() > 0) {
                                pane.setPage(pane.getPage() - 1);
                                gui.update();
                            }
                        }
                ),
                Slot.fromIndex(prevSlot)
        );

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.PAPER)
                                .name(msg("<yellow>Page " + (pane.getPage() + 1)))
                                .setLore(msg("<gray>" + browserLabel))
                                .build(),
                        event -> event.setCancelled(true)
                ),
                Slot.fromIndex(midSlot)
        );

        buttonPane.addItem(new GuiItem(
                        ItemBuilder.item(Material.ARROW)
                                .name(msg("<yellow>Next Page"))
                                .setLore(msg("<gray>Go to the next page"))
                                .build(),
                        event -> {
                            event.setCancelled(true);
                            if (pane.getPage() + 1 < pane.getPages()) {
                                pane.setPage(pane.getPage() + 1);
                                gui.update();
                            }
                        }
                ),
                Slot.fromIndex(nextSlot)
        );
    }
}