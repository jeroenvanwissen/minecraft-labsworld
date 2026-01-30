package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.commands.lw.AggroSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.AttackSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.DuelSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.HelpSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.LinkSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.LwSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.ReloadTwitchSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.SpawnSubcommand

class LwCommand(
    private val context: CommandContext,
) : Command<Map<String, Any>> {
    override val twitchClient: TwitchClient = context.twitchClient
    override val name = "lw"
    override val permission = Permission.EVERYONE
    override val type = CommandType.COMMAND
    override var storage: Map<String, Any> = emptyMap()

    override fun init() {
        // Initialization logic here
    }

    override fun handle(invocation: CommandInvocation) {
        val userName = invocation.userName
        val subcommandName = invocation.args.firstOrNull()?.lowercase()

        if (subcommandName == null) {
            invocation.reply("@${userName} Usage: !lw help")
            return
        }

        val subcommand = subcommandIndex[subcommandName]
        if (subcommand == null) {
            invocation.reply("@${userName} Unknown subcommand '$subcommandName'. Try: !lw help")
            return
        }

        subcommand.handle(invocation)
    }

    private val subcommands: List<LwSubcommand> by lazy {
        listOf(
            HelpSubcommand(context),
            ReloadTwitchSubcommand(context),
            LinkSubcommand(context),
            SpawnSubcommand(context),
            AggroSubcommand(context),
            AttackSubcommand(context),
            DuelSubcommand(context),
        )
    }

    private val subcommandIndex: Map<String, LwSubcommand> by lazy {
        val entries = subcommands.flatMap { cmd ->
            listOf(cmd.name).plus(cmd.aliases).map { it.lowercase() to cmd }
        }

        val dupes =
            entries
                .groupBy({ it.first }, { it.second })
                .filterValues { it.distinct().size > 1 }
                .keys

        check(dupes.isEmpty()) {
            "Duplicate !lw subcommand names/aliases found: ${dupes.sorted().joinToString(", ")}"
        }

        entries.toMap()
    }
}
