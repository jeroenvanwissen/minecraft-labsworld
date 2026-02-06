package nl.jeroenlabs.labsWorld.twitch.actions

data class ActionConfig(
    val type: String,
    val params: Map<String, Any?>,
)

data class ActionContext(
    val plugin: org.bukkit.plugin.java.JavaPlugin,
    val twitchClient: com.github.twitch4j.TwitchClient,
    val twitchConfigManager: nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager,
)

data class ActionInvocation(
    val userId: String,
    val userName: String,
    val channelName: String,
    val message: String?,
    val rewardId: String?,
    val rewardTitle: String?,
    val userInput: String?,
    val commandName: String?,
) {
    companion object {
        fun fromRedeem(invocation: nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation) =
            ActionInvocation(
                userId = invocation.userId,
                userName = invocation.userName,
                channelName = invocation.broadcasterUserName ?: invocation.userLogin ?: invocation.userName,
                message = invocation.userInput,
                rewardId = invocation.rewardId,
                rewardTitle = invocation.rewardTitle,
                userInput = invocation.userInput,
                commandName = null,
            )

        fun fromCommand(invocation: nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation) =
            ActionInvocation(
                userId = invocation.userId,
                userName = invocation.userName,
                channelName = invocation.channelName,
                message = invocation.message,
                rewardId = null,
                rewardTitle = null,
                userInput = null,
                commandName = invocation.commandName,
            )
    }
}
