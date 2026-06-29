package me.wega.wegapolls.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.config.ManualMorphiaConfig;
import lombok.Getter;
import me.wega.wegapolls.WegaPolls;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollPage;
import me.wega.wegapolls.poll.PollRegistry;
import me.wega.wegapolls.session.PollSessionRegistry;
import me.wega.wegapolls.session.PollStats;
import org.bson.UuidRepresentation;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public final class MongoService {

    private final WegaPolls plugin;
    private final Logger log;

    @Getter
    private MongoClient mongoClient;
    @Getter
    private Datastore datastore;

    public MongoService(WegaPolls plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
    }

    public void connect() {
        FileConfiguration cfg = plugin.getConfig();
        String uri = cfg.getString("mongodb.uri", "mongodb://localhost:27017");
        String dbName = cfg.getString("mongodb.database", "wegapolls");

        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(uri))
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .build()
        );

        datastore = Morphia.createDatastore(
                mongoClient,
                ManualMorphiaConfig.configure()
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .storeEmpties(true)
                        .storeNulls(true)
                        .database(dbName)
                        .applyIndexes(true),
                WegaPolls.class.getClassLoader()
        );

        datastore.getMapper().map(PollDefinition.class, PollPage.class, PollStats.class);
        datastore.ensureIndexes();

        log.info("[MongoService] Mapped " + datastore.getMapper().getMappedEntities().size() + " entities.");
        log.info("[MongoService] Connected to MongoDB database '" + dbName + "'.");
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            log.info("[MongoService] MongoDB connection closed.");
        }
    }

    public void load(PollRegistry pollRegistry, PollSessionRegistry sessionRegistry) {
        datastore.find(PollDefinition.class)
                .forEach(pollRegistry::register);

        datastore.find(PollStats.class)
                .forEach(sessionRegistry.getStatsRegistry()::load);
    }

    public void save(PollRegistry pollRegistry, PollSessionRegistry sessionRegistry) {
        if (!pollRegistry.all().isEmpty()) {
            datastore.save(pollRegistry.all());
        }

        datastore.save(sessionRegistry.getStatsRegistry().getAll());
    }
}