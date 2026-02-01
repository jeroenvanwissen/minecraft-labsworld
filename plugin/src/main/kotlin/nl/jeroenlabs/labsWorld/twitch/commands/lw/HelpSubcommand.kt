package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object HelpSubcommand : LwSubcommand {
    override val name = "help"

    override fun handle(ctx: CommandContext, inv: CommandInvocation) {
        inv.replyMention("Commands: spawn, duel @user, aggro <player>, attack <player> [seconds] [hearts], reload")
    }
}
