package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class HelpSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "help"

    override fun handle(invocation: CommandInvocation) {
        invocation.reply("@${invocation.userName} Help is on the way...")
    }
}
