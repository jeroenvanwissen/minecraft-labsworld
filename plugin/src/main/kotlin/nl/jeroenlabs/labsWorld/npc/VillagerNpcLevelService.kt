package nl.jeroenlabs.labsWorld.npc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin

class VillagerNpcLevelService(
    private val plugin: JavaPlugin,
    private val npcLinkManager: VillagerNpcLinkManager,
) {
    companion object {
        const val XP_PER_LEVEL_BASE = 100
        const val DUEL_WIN_XP = 50
        const val DUEL_PARTICIPATION_XP = 10
    }

    fun getLevel(villager: Villager): Int = VillagerNpcKeys.getLevel(villager, plugin)

    fun getXp(villager: Villager): Int = VillagerNpcKeys.getXp(villager, plugin)

    fun xpForNextLevel(currentLevel: Int): Int = currentLevel * XP_PER_LEVEL_BASE

    fun addXp(villager: Villager, amount: Int): LevelUpResult {
        val oldLevel = getLevel(villager)
        val newXp = getXp(villager) + amount
        VillagerNpcKeys.setXp(villager, newXp, plugin)

        var level = oldLevel
        while (newXp >= totalXpForLevel(level + 1)) {
            level++
        }

        val leveledUp = level > oldLevel
        if (leveledUp) {
            VillagerNpcKeys.setLevel(villager, level, plugin)
            updateNameTag(villager, level)
            playLevelUpEffects(villager)
        }

        return LevelUpResult(
            oldLevel = oldLevel,
            newLevel = level,
            totalXp = newXp,
            leveledUp = leveledUp,
        )
    }

    fun updateNameTag(villager: Villager, level: Int? = null) {
        val userId = VillagerNpcKeys.getLinkedUserId(villager, plugin) ?: return
        val lvl = level ?: getLevel(villager)
        val baseName = npcLinkManager.getStoredUserName(userId) ?: userId

        villager.customName(
            Component.text("[Lv.$lvl] ", NamedTextColor.GOLD)
                .append(Component.text(baseName, NamedTextColor.WHITE)),
        )
        villager.isCustomNameVisible = true
    }

    fun totalXpForLevel(level: Int): Int {
        var total = 0
        for (l in 1 until level) {
            total += l * XP_PER_LEVEL_BASE
        }
        return total
    }

    private fun playLevelUpEffects(villager: Villager) {
        if (!villager.isValid) return
        val loc = villager.location
        loc.world?.spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.0, 1.5, 0.0), 20, 0.5, 0.5, 0.5)
        loc.world?.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }

    data class LevelUpResult(
        val oldLevel: Int,
        val newLevel: Int,
        val totalXp: Int,
        val leveledUp: Boolean,
    )
}
