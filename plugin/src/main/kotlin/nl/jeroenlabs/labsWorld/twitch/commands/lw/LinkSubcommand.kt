package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class LinkSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "link"

    override fun handle(invocation: CommandInvocation) {
        // Intentionally left blank (preserves current behavior).
    }
}
