package sciwhiz12.janitor.utils;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {
    public static <T> T make(Supplier<T> creator, Consumer<T> configurator) {
        T obj = creator.get();
        configurator.accept(obj);
        return obj;
    }

//    public static String toString(final MessageAuthor author) {
//        return author.asUser().map(Util::toString).orElseGet(() -> String.format("{MessageAuthor,%s}:%s", author.getDiscriminatedName(), author.getId()));
//    }

    public static String toString(@Nullable final User user) {
        return user != null ? String.format("{User,%s#%s}:%s", user.getName(), user.getDiscriminator(), getID(user)) : "unknown";
    }

    public static String toString(final MessageChannel channel) {
        if (channel instanceof GuildChannel) {
            GuildChannel gc = (GuildChannel) channel;
            return String.format("[GuildChannel:%s,%s@%s]%s", gc.getType(), gc.getName(), toString(gc.getGuild()), getID(channel));
        }
        // TextChannel vs PrivateChannel
        return String.format("[MessageChannel]:%s", getID(channel));
    }

    public static String toString(final Guild guild) {
        return String.format("(Guild:%s):%s", guild.getName(), getID(guild));
    }

    public static String getID(final ISnowflake entity) {
        String prefix = "?";
        if (entity instanceof User) {
            prefix = "@";
        } else if (entity instanceof Role) {
            prefix = "@&";
        } else if (entity instanceof GuildChannel) {
            prefix = "#";
        }
        return String.format("<%s%s>", prefix, entity.getId());
    }

    public static String nameFor(User user) {
        return user.getName().concat("#").concat(user.getDiscriminator());
    }

    public static <Success, Error> BiConsumer<Success, Error> handle(final Consumer<Success> success,
                                                                     final Consumer<Error> exceptionally) {
        return (suc, ex) -> {
            if (ex == null) {
                success.accept(suc);
            } else {
                exceptionally.accept(ex);
            }
        };
    }
}
