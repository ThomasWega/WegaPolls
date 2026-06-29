package me.wega.wegapolls;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import lombok.Getter;
import me.wega.wegapolls.cmd.AdminPollsCmd;
import me.wega.wegapolls.cmd.PollsCmd;
import me.wega.wegapolls.input.ChatInputListener;
import me.wega.wegapolls.input.ChatInputService;
import me.wega.wegapolls.mongo.MongoService;
import me.wega.wegapolls.poll.PollRegistry;
import me.wega.wegapolls.session.PollSessionRegistry;
import me.wega.wegapolls.session.PollStatsRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class WegaPolls extends JavaPlugin {
    public static WegaPolls INSTANCE;

    private ChatInputService chatInputService;
    private final PollRegistry pollRegistry = new PollRegistry();
    private final PollStatsRegistry pollStatsRegistry = new PollStatsRegistry();
    private final PollSessionRegistry pollSessionRegistry = new PollSessionRegistry(pollStatsRegistry);
    private MongoService mongoService;

    @Override
    public void onLoad() {
        INSTANCE = this;
        CommandAPI.onLoad(new CommandAPIPaperConfig(this));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        saveDefaultConfig();

        mongoService = new MongoService(this);
        mongoService.connect();
        mongoService.load(pollRegistry, pollSessionRegistry);

        chatInputService = new ChatInputService();
        Bukkit.getPluginManager().registerEvents(new ChatInputListener(chatInputService), this);
        new AdminPollsCmd(this, pollRegistry);
        new PollsCmd(pollRegistry, pollSessionRegistry);
    }

    @Override
    public void onDisable() {
        if (mongoService != null) {
            mongoService.save(pollRegistry, pollSessionRegistry);
            mongoService.disconnect();
        }
        CommandAPI.onDisable();
    }
}