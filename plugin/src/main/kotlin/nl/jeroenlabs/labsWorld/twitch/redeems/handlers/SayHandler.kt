package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import nl.jeroenlabs.labsWorld.twitch.redeems.pluginAsLabsWorld
import nl.jeroenlabs.labsWorld.twitch.redeems.renderTemplate

class Say : RedeemHandler {
    override val key: String = "chat.say"
    override val runOnMainThread: Boolean = false

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        pluginAsLabsWorld(context.plugin) ?: return
        val message = params["message"] as? String ?: return
        val rendered = renderTemplate(message, invocation)
        context.say(rendered)
    }
}
