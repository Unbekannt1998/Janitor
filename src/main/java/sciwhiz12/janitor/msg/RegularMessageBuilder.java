package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.msg.json.RegularMessage;
import sciwhiz12.janitor.msg.substitution.IHasCustomSubstitutions;
import sciwhiz12.janitor.msg.substitution.SubstitutionMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegularMessageBuilder implements IHasCustomSubstitutions<RegularMessageBuilder> {
    private final RegularMessage message;
    private final Map<String, Supplier<String>> customSubstitutions;

    public RegularMessageBuilder(RegularMessage message, Map<String, Supplier<String>> customSubstitutions) {
        this.message = message;
        this.customSubstitutions = customSubstitutions;
    }

    public RegularMessageBuilder(RegularMessage message) {
        this(message, new HashMap<>());
    }

    public RegularMessageBuilder(RegularMessageBuilder copy) {
        this(copy.message, new HashMap<>(copy.customSubstitutions));
    }

    public RegularMessageBuilder apply(Consumer<RegularMessageBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public RegularMessageBuilder with(final String argument, final Supplier<String> value) {
        customSubstitutions.put(argument, value);
        return this;
    }

    public MessageEmbed build(TranslationMap translations, SubstitutionMap substitutions) {
        return message.create(translations, substitutions.with(customSubstitutions)).build();
    }

    public MessageEmbed build(JanitorBot bot) {
        return build(bot.getTranslations(), bot.getSubstitutions());
    }

    public MessageAction send(JanitorBot bot, MessageChannel channel) {
        return channel.sendMessage(build(bot));
    }
}
