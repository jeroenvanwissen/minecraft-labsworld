package nl.jeroenlabs.labsWorld.twitch.redeems

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class RedeemDispatcher(
    private val plugin: JavaPlugin,
    private val twitchClient: TwitchClient,
    private val twitchConfigManager: TwitchConfigManager,
) {
    private val handlers = ConcurrentHashMap<String, RedeemHandler>()
    private val lastAtMsByUserAndBinding = ConcurrentHashMap<String, Long>()

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

        val handler = handlers[binding.handler.lowercase()]
        if (handler == null) {
            plugin.logger.warning(
                "Redeem binding matched rewardId='${invocation.rewardId}' title='${invocation.rewardTitle}', but handler '${binding.handler}' is not registered. Registered=${handlers.keys}",
            )
            return
        }

        val cooldownMs = binding.cooldownMs ?: 0L
        if (cooldownMs > 0) {
            val rewardKey = binding.rewardId ?: binding.rewardTitle ?: invocation.rewardId
            val key = "${invocation.userId}:${rewardKey.lowercase()}"
            val now = System.currentTimeMillis()
            val last = lastAtMsByUserAndBinding[key]
            if (last != null && now - last < cooldownMs) {
                return
            }
            lastAtMsByUserAndBinding[key] = now
        }

        val runner = Runnable {
            val context = RedeemHandlerContext(plugin, twitchClient, twitchConfigManager)
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

        val shouldMainThread = binding.runOnMainThread ?: handler.runOnMainThread
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
            val idMatch = b.rewardId?.equals(invocation.rewardId, ignoreCase = true) == true
            val titleMatch = b.rewardTitle?.equals(invocation.rewardTitle, ignoreCase = true) == true
            idMatch || titleMatch
        }
    }

}
