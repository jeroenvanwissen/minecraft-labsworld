package nl.jeroenlabs.labsWorld.npc

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

class NpcSpawnPointListener(
    private val manager: NpcSpawnPointManager,
) : Listener {
    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (!manager.isSpawnPointItem(event.itemInHand)) return

        val player = event.player
        if (!manager.canUseSpawnPoints(player)) {
            event.isCancelled = true
            player.sendMessage("You do not have permission to place NPC Spawn Points.")
            return
        }

        runCatching {
            manager.markPlacedBlock(event.blockPlaced)
        }.onFailure {
            event.isCancelled = true
            player.sendMessage("Failed to place NPC Spawn Point: ${it.message ?: it::class.simpleName}")
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (!manager.isSpawnPointBlock(event.block)) return

        val player = event.player
        if (!manager.canUseSpawnPoints(player)) {
            event.isCancelled = true
            player.sendMessage("You do not have permission to break NPC Spawn Points.")
            return
        }

        event.isDropItems = false
        event.expToDrop = 0

        manager.unmarkBrokenBlock(event.block)
        event.block.world.dropItemNaturally(event.block.location.add(0.5, 0.5, 0.5), manager.createSpawnPointItem())
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeIf { manager.isSpawnPointBlock(it) }
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { manager.isSpawnPointBlock(it) }
    }
}
