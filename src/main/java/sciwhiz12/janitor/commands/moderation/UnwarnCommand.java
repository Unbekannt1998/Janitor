package sciwhiz12.janitor.commands.moderation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;
import sciwhiz12.janitor.moderation.warns.WarningStorage;
import sciwhiz12.janitor.msg.MessageHelper;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;

import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;

public class UnwarnCommand extends BaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public UnwarnCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("unwarn")
            .requires(ctx -> getBot().getConfig().WARNINGS_ENABLE.get())
            .then(argument("caseId", IntegerArgumentType.integer(1))
                .executes(this::run)
            );
    }

    public int run(CommandContext<MessageReceivedEvent> ctx) {
        realRun(ctx);
        return 1;
    }

    void realRun(CommandContext<MessageReceivedEvent> ctx) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel).queue();

            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        int caseID = IntegerArgumentType.getInteger(ctx, "caseId");

        if (!performer.hasPermission(WARN_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", WARN_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else {
            final WarningStorage storage = WarningStorage.get(getBot().getStorage(), guild);
            @Nullable
            final WarningEntry entry = storage.getWarning(caseID);
            Member temp;
            if (entry == null) {
                messages().getRegularMessage("moderation/error/unwarn/no_case_found")
                    .apply(MessageHelper.member("performer", performer))
                    .with("case_id", () -> String.valueOf(caseID))
                    .send(getBot(), channel).queue();

            } else if (entry.getWarned().getIdLong() == performer.getIdLong()
                && !config().WARNINGS_REMOVE_SELF_WARNINGS.get()) {
                messages().getRegularMessage("moderation/error/unwarn/cannot_unwarn_self")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.warningEntry("warning_entry", caseID, entry))
                    .send(getBot(), channel).queue();

            } else if (config().WARNINGS_RESPECT_MOD_ROLES.get()
                && (temp = guild.getMember(entry.getPerformer())) != null && !performer.canInteract(temp)) {
                messages().getRegularMessage("moderation/error/unwarn/cannot_remove_higher_mod")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.warningEntry("warning_entry", caseID, entry))
                    .send(getBot(), channel).queue();

            } else {
                storage.removeWarning(caseID);
                messages().getRegularMessage("moderation/unwarn/info")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.warningEntry("warning_entry", caseID, entry))
                    .send(getBot(), channel).queue();

            }
        }
    }
}
