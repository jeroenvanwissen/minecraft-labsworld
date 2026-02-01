package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation

object ChatSayHandler : RedeemHandler {
    override val key = "chat.say"
    override val runOnMainThread = false

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val message = params["message"] as? String ?: return
        context.say(renderTemplate(message, invocation))
    }
}
