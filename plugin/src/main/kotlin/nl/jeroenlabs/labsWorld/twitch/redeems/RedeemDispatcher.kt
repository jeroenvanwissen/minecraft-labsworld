package nl.jeroenlabs.labsWorld.twitch.redeems

import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionExecutor
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class RedeemDispatcher(
    private val context: TwitchContext,
) {
    private val plugin get() = context.plugin
    private val twitchConfigManager get() = context.twitchConfigManager

    private val handlers = ConcurrentHashMap<String, RedeemHandler>()

    fun register(handler: RedeemHandler) {
        handlers[handler.key.lowercase()] = handler
    }

    fun handle(event: ChannelPointsCustomRewardRedemptionEvent) {
        if (!twitchConfigManager.isRedeemsEnabled()) return

        val invocation = RedeemInvocation.fromEvent(event)
        if (invocation == null) {
            plugin.logger.warning("Redeem received but could not parse payload (missing fields)")
            return
        }

        val binding = findBinding(invocation)
        if (binding == null) {
            if (twitchConfigManager.shouldLogUnmatchedRedeems()) {
                plugin.logger.info(
                    "Unmatched redeem: reward='${invocation.rewardTitle}' rewardId='${invocation.rewardId}' user='${invocation.userName}' input='${invocation.userInput ?: ""}'",
                )
            }
            return
        }

        val handler = binding.handler?.let { handlers[it.lowercase()] }
        val hasActions = binding.actions.isNotEmpty()
        if (!hasActions && handler == null) {
            plugin.logger.warning(
                "Redeem binding matched rewardId='${invocation.rewardId}' title='${invocation.rewardTitle}', but handler '${binding.handler}' is not registered. Registered=${handlers.keys}",
            )
            return
        }

        plugin.logger.warning("Matched redeem for reward='${invocation.rewardTitle}' user='${invocation.userName}' handler='${handler?.key}' actions=${binding.actions.size}")

        val runner = Runnable {
            if (hasActions) {
                val actionInvocation = ActionInvocation.fromRedeem(invocation)
                runCatching {
                    ActionExecutor.executeActions(context, actionInvocation, binding.actions)
                }.onFailure { err ->
                    plugin.logger.log(
                        Level.WARNING,
                        "Redeem actions failed reward='${invocation.rewardTitle}' user='${invocation.userName}'",
                        err,
                    )
                }
            } else if (handler != null) {
                runCatching {
                    handler.handle(context, invocation, binding.params)
                }.onFailure { err ->
                    plugin.logger.log(
                        Level.WARNING,
                        "Redeem handler failed handler='${handler.key}' reward='${invocation.rewardTitle}' user='${invocation.userName}'",
                        err,
                    )
                }
            }
        }

        val shouldMainThread = binding.runOnMainThread ?: (handler?.runOnMainThread ?: true)
        if (shouldMainThread) {
            plugin.server.scheduler.runTask(plugin, runner)
        } else {
            plugin.server.scheduler.runTaskAsynchronously(plugin, runner)
        }
    }

    private fun findBinding(invocation: RedeemInvocation): TwitchConfigManager.RedeemBindingConfig? {
        val bindings = twitchConfigManager.getRedeemBindings()
        if (bindings.isEmpty()) return null

        return bindings.firstOrNull { b ->
            plugin.logger.info("Checking redeem binding for rewardId='${b.rewardId}' title='${b.rewardTitle}'")
            val idMatch = b.rewardId?.equals(invocation.rewardId, ignoreCase = true) == true
            val titleMatch = b.rewardTitle?.equals(invocation.rewardTitle, ignoreCase = true) == true
            idMatch || titleMatch
        }
    }

}
