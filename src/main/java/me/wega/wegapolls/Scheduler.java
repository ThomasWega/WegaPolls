package me.wega.wegapolls;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Scheduler {

    public static void runSync(Runnable r) {
        Bukkit.getScheduler().runTask(WegaPolls.INSTANCE, r);
    }
}
