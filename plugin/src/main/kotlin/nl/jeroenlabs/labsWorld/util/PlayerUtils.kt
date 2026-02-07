package nl.jeroenlabs.labsWorld.util

import org.bukkit.Server
import org.bukkit.entity.Player
import kotlin.random.Random

object PlayerUtils {
    /**
     * Pick a target player by name, or fall back to a random online player.
     *
     * @param server      the Bukkit server instance
     * @param preferred   exact player name to look up (nullable)
     * @param allowRandom if true, pick a random online player when [preferred] is absent or not found
     * @return the resolved [Player], or null if no suitable player is available
     */
    fun pickTargetPlayer(server: Server, preferred: String?, allowRandom: Boolean): Player? {
        val online = server.onlinePlayers.toList()
        if (!preferred.isNullOrBlank()) {
            val exact = server.getPlayerExact(preferred)
            if (exact != null) return exact
            if (!allowRandom) return null
        }
        if (online.isEmpty()) return null
        if (online.size == 1) return online.first()
        if (!allowRandom) return null
        return online[Random.nextInt(online.size)]
    }
}
