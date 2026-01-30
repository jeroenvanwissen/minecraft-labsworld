package nl.jeroenlabs.labsWorld.npc

import nl.jeroenlabs.labsWorld.LabsWorld
import org.bukkit.entity.Player

internal fun ensureLinkedNpcAtSpawnPoint(plugin: LabsWorld, userId: String, userName: String): Result<String> {
    val spawnPoint = plugin.pickNpcSpawnPointSpawnLocation()
        ?: return Result.failure(IllegalStateException("No NPC Spawn Point is placed"))
    return plugin.ensureNpcAtSpawnPoint(userId, userName, spawnPoint)
}

internal fun aggroLinkedNpcs(plugin: LabsWorld, target: Player, seconds: Int): Result<Int> =
    plugin.startAggroAllNpcs(target, seconds)

internal fun attackLinkedNpcs(plugin: LabsWorld, target: Player, seconds: Int, hearts: Double): Result<Int> =
    plugin.startAttackAllNpcs(target, seconds, hearts)
