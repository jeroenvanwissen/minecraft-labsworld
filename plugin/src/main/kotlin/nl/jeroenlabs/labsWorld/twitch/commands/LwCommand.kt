package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.commands.lw.LwSubcommand
import nl.jeroenlabs.labsWorld.twitch.commands.lw.LwSubcommands

/**
 * Main !lw command that delegates to subcommands.
 */
class LwCommand(
    private val context: CommandContext,
) : Command<Unit> {
    override val twitchClient: TwitchClient = context.twitchClient
    override val name = "lw"
    override val permission = Permission.EVERYONE
    override val type = CommandType.COMMAND
    override var storage: Unit = Unit

    override fun init() {}

    override fun handle(invocation: CommandInvocation) {
        val subName = invocation.args.firstOrNull()?.lowercase()

        if (subName == null) {
            invocation.replyMention("Usage: !lw help")
            return
        }

        val subcommand = subcommandIndex[subName]
        if (subcommand == null) {
            invocation.replyMention("Unknown command '$subName'. Try: !lw help")
            return
        }

        subcommand.handle(context, invocation)
    }

    private val subcommandIndex: Map<String, LwSubcommand> by lazy {
        LwSubcommands.all.flatMap { cmd ->
            (listOf(cmd.name) + cmd.aliases).map { it.lowercase() to cmd }
        }.toMap()
    }
}
