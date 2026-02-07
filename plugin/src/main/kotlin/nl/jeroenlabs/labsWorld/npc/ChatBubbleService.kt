package nl.jeroenlabs.labsWorld.npc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.Villager
import org.bukkit.plugin.Plugin

/**
 * Displays floating chat-bubble [TextDisplay] entities above villager NPCs
 * when at least one player is nearby.
 */
class ChatBubbleService(private val plugin: Plugin) {

    /**
     * Shows a chat bubble above [villager] when a player is within [radius] blocks.
     * The bubble is automatically removed after [ttlTicks].
     */
    fun showBubble(
        villager: Villager,
        message: String,
        radius: Double = 15.0,
        checkIntervalTicks: Long = 1L,
        ttlTicks: Long = 20L * 6L,
    ) {
        var display: TextDisplay? = null
        var elapsed = 0L

        var task: org.bukkit.scheduler.BukkitTask? = null
        task = Bukkit.getScheduler().runTaskTimer(
            plugin,
            Runnable {
                elapsed += checkIntervalTicks
                if (!villager.isValid || elapsed >= ttlTicks) {
                    display?.remove()
                    task?.cancel()
                    return@Runnable
                }

                val hasNearbyPlayer = villager.location.world?.players?.any { player ->
                    player.location.distanceSquared(villager.location) <= radius * radius
                } == true

                if (hasNearbyPlayer) {
                    val anchor = villager.location.clone().apply {
                        y = villager.boundingBox.maxY + 0.6
                    }

                    if (display == null || !display!!.isValid) {
                        display = villager.world.spawn(
                            anchor,
                            TextDisplay::class.java,
                        ) {
                            it.text(Component.text(message, NamedTextColor.BLACK))
                            it.setBackgroundColor(Color.WHITE)
                            it.billboard = Display.Billboard.CENTER
                            it.isPersistent = false
                        }
                    } else {
                        display!!.teleport(anchor)
                    }
                } else {
                    display?.remove()
                    display = null
                }
            },
            0L,
            checkIntervalTicks,
        )
    }
}
