package sciwhiz12.janitor.moderation.warns;

import com.electronwill.nightconfig.core.utils.ObservedMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.entities.Guild;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.storage.GuildStorage;
import sciwhiz12.janitor.storage.JsonStorage;
import sciwhiz12.janitor.storage.StorageKey;

import java.util.HashMap;
import java.util.Map;

public class WarningStorage extends JsonStorage {
    private static final TypeReference<Map<Integer, WarningEntry>> WARNING_MAP_TYPE = new TypeReference<>() {};
    public static final StorageKey<WarningStorage> KEY = new StorageKey<>("warnings", WarningStorage.class);

    public static WarningStorage get(GuildStorage storage, Guild guild) {
        return storage.getOrCreate(guild, KEY, () -> new WarningStorage(storage.getBot()));
    }

    private final JanitorBot bot;
    private int lastID = 1;
    private final Map<Integer, WarningEntry> warnings = new ObservedMap<>(new HashMap<>(), this::markDirty);

    public WarningStorage(JanitorBot bot) {
        this.bot = bot;
    }

    public JanitorBot getBot() {
        return bot;
    }

    public int addWarning(WarningEntry entry) {
        int id = lastID++;
        warnings.put(id, entry);
        return id;
    }

    @Nullable
    public WarningEntry getWarning(int caseID) {
        return warnings.get(caseID);
    }

    public void removeWarning(int caseID) {
        warnings.remove(caseID);
    }

    public Map<Integer, WarningEntry> getWarnings() {
        return warnings;
    }

    @Override
    protected void initialize(ObjectMapper mapper) {
        super.initialize(mapper);
        mapper.registerModule(
            new SimpleModule()
                .addSerializer(WarningEntry.class, new WarningEntry.Serializer())
                .addDeserializer(WarningEntry.class, new WarningEntry.Deserializer(this::getBot))
        );
    }

    @Override
    public JsonNode save(ObjectMapper mapper) {
        final ObjectNode obj = mapper.createObjectNode();
        obj.put("lastCaseID", lastID);
        obj.set("warnings", mapper.valueToTree(warnings));
        return obj;
    }

    @Override
    public void load(JsonNode in, ObjectMapper mapper) {
        lastID = in.get("lastCaseID").asInt();
        final Map<Integer, WarningEntry> loaded = mapper.convertValue(in.get("warnings"), WARNING_MAP_TYPE);
        warnings.clear();
        warnings.putAll(loaded);
    }
}
