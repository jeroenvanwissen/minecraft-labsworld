package nl.jeroenlabs.labsWorld.twitch.redeems

import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent

data class RedeemInvocation(
    val rewardId: String,
    val rewardTitle: String,
    val rewardCost: Int?,
    val rewardPrompt: String?,
    val redemptionId: String?,
    val userId: String,
    val userLogin: String?,
    val userName: String,
    val userInput: String?,
    val broadcasterUserId: String?,
    val broadcasterUserName: String?,
    val raw: ChannelPointsCustomRewardRedemptionEvent,
) {
    companion object {
        fun fromEvent(event: ChannelPointsCustomRewardRedemptionEvent): RedeemInvocation? {
            // Twitch4J has changed the shape/naming of EventSub event payloads over time.
            // To keep this plugin resilient across minor Twitch4J updates, we extract fields via reflection.

            fun call0(target: Any?, vararg methodNames: String): Any? {
                if (target == null) return null
                val methods = target.javaClass.methods
                for (name in methodNames) {
                    val m = methods.firstOrNull { it.parameterCount == 0 && it.name == name }
                    if (m != null) {
                        return runCatching { m.invoke(target) }.getOrNull()
                    }
                }
                return null
            }

            fun callAnyGetter(target: Any?, vararg logicalNames: String): Any? {
                if (target == null) return null
                for (logical in logicalNames) {
                    val cap = logical.replaceFirstChar { it.uppercaseChar() }
                    val value = call0(target, "get$cap", logical)
                    if (value != null) return value
                }
                return null
            }

            fun asString(value: Any?): String? =
                when (value) {
                    null -> null
                    is String -> value
                    else -> value.toString()
                }?.trim()?.takeIf { it.isNotEmpty() }

            fun asInt(value: Any?): Int? =
                when (value) {
                    is Int -> value
                    is Long -> value.toInt()
                    is Number -> value.toInt()
                    is String -> value.trim().toIntOrNull()
                    else -> null
                }

            // Some versions expose the payload as getEvent(), others as getData() or getPayload().
            val payload = callAnyGetter(event, "event", "data", "payload") ?: event
            val reward = callAnyGetter(payload, "reward") ?: return null

            val rewardId = asString(callAnyGetter(reward, "id")) ?: return null
            val rewardTitle = asString(callAnyGetter(reward, "title")) ?: return null
            val rewardCost = asInt(callAnyGetter(reward, "cost"))
            val rewardPrompt = asString(callAnyGetter(reward, "prompt"))

            val redemptionId = asString(callAnyGetter(payload, "id", "redemptionId"))

            val userId = asString(callAnyGetter(payload, "userId"))
                ?: asString(callAnyGetter(payload, "userLogin"))
                ?: return null
            val userLogin = asString(callAnyGetter(payload, "userLogin"))
            val userName = asString(callAnyGetter(payload, "userName")) ?: userLogin ?: userId
            val userInput = asString(callAnyGetter(payload, "userInput"))

            val broadcasterUserId = asString(callAnyGetter(payload, "broadcasterUserId"))
            val broadcasterUserName = asString(callAnyGetter(payload, "broadcasterUserName"))

            return RedeemInvocation(
                rewardId = rewardId,
                rewardTitle = rewardTitle,
                rewardCost = rewardCost,
                rewardPrompt = rewardPrompt,
                redemptionId = redemptionId,
                userId = userId,
                userLogin = userLogin,
                userName = userName,
                userInput = userInput,
                broadcasterUserId = broadcasterUserId,
                broadcasterUserName = broadcasterUserName,
                raw = event,
            )
        }
    }
}
