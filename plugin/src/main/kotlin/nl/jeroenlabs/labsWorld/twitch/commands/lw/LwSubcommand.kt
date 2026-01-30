package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

interface LwSubcommand {
    val name: String
    val aliases: Set<String>
        get() = emptySet()

    fun handle(invocation: CommandInvocation)
}
