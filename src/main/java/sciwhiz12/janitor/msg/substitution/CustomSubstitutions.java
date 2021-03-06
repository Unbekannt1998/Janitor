package sciwhiz12.janitor.msg.substitution;

import org.apache.commons.collections4.TransformerUtils;
import org.apache.commons.collections4.map.DefaultedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomSubstitutions implements ISubstitutor, IHasCustomSubstitutions<CustomSubstitutions> {
    private final Map<String, Supplier<String>> map;

    public CustomSubstitutions(Map<String, Supplier<String>> map) {
        this.map = map;
    }

    public CustomSubstitutions() {
        this(new HashMap<>());
    }

    @Override
    public String substitute(String text) {
        return SubstitutionMap.substitute(text, map);
    }

    public CustomSubstitutions apply(Consumer<CustomSubstitutions> consumer) {
        consumer.accept(this);
        return this;
    }

    public CustomSubstitutions with(final String argument, final Supplier<String> value) {
        map.put(argument, value);
        return this;
    }

    public CustomSubstitutions with(Map<String, Supplier<String>> customSubstitutions) {
        return new CustomSubstitutions(createDefaultedMap(customSubstitutions));
    }

    public Map<String, Supplier<String>> createDefaultedMap(Map<String, Supplier<String>> custom) {
        return DefaultedMap.defaultedMap(custom, TransformerUtils.mapTransformer(map));
    }

    public Map<String, Supplier<String>> getMap() {
        return map;
    }
}
