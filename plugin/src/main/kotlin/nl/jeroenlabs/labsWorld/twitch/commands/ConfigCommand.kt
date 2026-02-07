package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.actions.ActionExecutor
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation

class ConfigCommand(
    private val context: TwitchContext,
    private val binding: TwitchConfigManager.CommandBindingConfig,
) : Command {
    override val twitchClient: TwitchClient = context.twitchClient
    override val name: String = binding.name
    override val permission: Permission = binding.permission
    override val type: CommandType = CommandType.COMMAND

    override val runOnMainThread: Boolean = binding.runOnMainThread ?: true

    override fun init() {}

    override fun handle(invocation: CommandInvocation) {
        val actionInvocation = ActionInvocation.fromCommand(invocation)
        ActionExecutor.executeActions(context, actionInvocation, binding.actions)
    }
}
