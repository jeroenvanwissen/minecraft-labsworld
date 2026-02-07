package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import org.bukkit.inventory.ItemStack
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import kotlin.math.min

class DropItemsActionHandler : ActionHandler {
    override val type: String = "player.drop_items"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val items = ActionUtils.parseItemStacks(params["items"])
        if (items.isEmpty()) error("No items defined")

        items.forEach { stack ->
            val maxStack = stack.maxStackSize.coerceAtLeast(1)
            var remaining = stack.amount
            while (remaining > 0) {
                val size = min(remaining, maxStack)
                val dropStack = ItemStack(stack.type, size)
                player.world.dropItemNaturally(player.location, dropStack)
                remaining -= size
            }
        }
    }
}
