package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object LevelSubcommand : LwSubcommand {
    override val name = "level"
    override val aliases = setOf("lvl")

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        val plugin = ctx.labsWorld()

        val npc = plugin.getNpcByUserId(inv.userId)
        if (npc == null) {
            return inv.replyMention("You don't have an NPC yet. Redeem the NPC spawn channel point reward first.")
        }

        val level = plugin.getNpcLevel(npc)
        val totalXp = plugin.getNpcXp(npc)
        val xpForNextLevel = plugin.getNpcXpForNextLevel(level)
        val xpIntoCurrentLevel = totalXp - plugin.getNpcTotalXpForLevel(level)

        inv.replyMention("Your NPC is Lv.$level ($xpIntoCurrentLevel/$xpForNextLevel XP to next level)")
    }
}
