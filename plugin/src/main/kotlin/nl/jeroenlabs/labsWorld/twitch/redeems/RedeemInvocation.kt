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
            // Twitch4J 1.25.0 exposes all fields via direct getters — no reflection needed.
            val reward = event.reward ?: return null

            val rewardId = reward.id ?: return null
            val rewardTitle = reward.title ?: return null
            val rewardCost = reward.cost
            val rewardPrompt = reward.prompt?.takeIf { it.isNotBlank() }

            val redemptionId = event.id

            val userId = event.userId
                ?: event.userLogin
                ?: return null
            val userLogin = event.userLogin
            val userName = event.userName ?: userLogin ?: userId
            val userInput = event.userInput?.takeIf { it.isNotBlank() }

            val broadcasterUserId = event.broadcasterUserId
            val broadcasterUserName = event.broadcasterUserName

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
