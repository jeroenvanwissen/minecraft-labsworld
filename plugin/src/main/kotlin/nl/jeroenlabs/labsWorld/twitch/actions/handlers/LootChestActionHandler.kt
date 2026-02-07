package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.anyToBool
import nl.jeroenlabs.labsWorld.util.anyToDouble
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString
import kotlin.random.Random

class LootChestActionHandler : ActionHandler {
    override val type: String = "world.loot_chest"

    /** Also handles "player.loot_chest" — registered under both keys via [additionalTypes]. */
    val additionalTypes: List<String> = listOf("player.loot_chest")

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return

        val radius = anyToInt(params["radius"], 6).coerceIn(1, 30)
        val tries = anyToInt(params["tries"], 24).coerceIn(1, 200)
        val rolls = anyToInt(params["rolls"], 4).coerceIn(1, 27)
        val announce = anyToBool(params["announce"], true) ?: true

        val debugForceBreadAmount = anyToInt(params["debug_force_bread_amount"], -1)

        val lootRaw = params["loot"] ?: params["items"] ?: params["loot_table"]
        val loot = parseLootEntries(lootRaw)
        if (loot.isEmpty() && debugForceBreadAmount <= 0) error("No loot defined")

        val chestName = anyToString(params["name"])?.takeIf { it.isNotBlank() } ?: "Loot Box"

        val spawnLocation = findChestSpawnLocation(player.location, radius, tries)
            ?: error("Could not find a safe spot to place a chest")

        val block = spawnLocation.block
        block.type = Material.CHEST

        val state = block.state as? Chest ?: error("Placed chest but block state is not a chest")
        runCatching { setChestNameBestEffort(state, chestName) }
        state.update(true, false)

        val fillRunner = Runnable {
            val liveChest = block.state as? Chest
            val inv = liveChest?.blockInventory
            if (inv == null) {
                context.plugin.logger.warning("Loot chest fill failed: chest inventory not available")
                return@Runnable
            }

            inv.clear()

            var placed = 0
            if (debugForceBreadAmount > 0) {
                val breadStack = ItemStack(Material.BREAD, debugForceBreadAmount.coerceAtLeast(1))
                if (placeInRandomEmptySlot(inv, breadStack)) placed += 1
                context.plugin.logger.info(
                    "Loot chest debug: forced BREADx${breadStack.amount}",
                )
            }

            if (loot.isNotEmpty()) {
                repeat(rolls) {
                    val entry = pickWeighted(loot) ?: return@repeat
                    val amount =
                        if (entry.minAmount >= entry.maxAmount) entry.minAmount
                        else Random.nextInt(entry.minAmount, entry.maxAmount + 1)

                    val stack = ItemStack(entry.material, amount.coerceAtLeast(1))
                    if (placeInRandomEmptySlot(inv, stack)) {
                        placed += 1
                    }
                }
            }

            val stackCount = inv.contents.count { it != null && it.type != Material.AIR }
            context.plugin.logger.info(
                "Loot chest spawned near ${player.name}: placed=$placed rolls=$rolls lootEntries=${loot.size} at ${block.x},${block.y},${block.z} stacks=$stackCount",
            )
        }

        context.plugin.server.scheduler.runTask(context.plugin, fillRunner)

        if (announce) {
            context.twitchClient.chat.sendMessage(
                invocation.channelName,
                "A $chestName appeared near ${player.name}! (x=${block.x} y=${block.y} z=${block.z})",
            )
        }
    }

    // -- Private helpers --

    private data class LootEntry(
        val material: Material,
        val minAmount: Int,
        val maxAmount: Int,
        val weight: Double,
    )

    private fun findChestSpawnLocation(origin: Location, radius: Int, tries: Int): Location? {
        val world = origin.world ?: return null
        val baseX = origin.blockX
        val baseZ = origin.blockZ

        repeat(tries) {
            val dx = Random.nextInt(-radius, radius + 1)
            val dz = Random.nextInt(-radius, radius + 1)
            val x = baseX + dx
            val z = baseZ + dz

            val highestY = world.getHighestBlockYAt(x, z)
            val placeY = highestY + 1
            if (placeY >= world.maxHeight) return@repeat

            val floor = world.getBlockAt(x, highestY, z)
            if (!floor.type.isSolid) return@repeat
            if (floor.type == Material.HOPPER) return@repeat

            val place = world.getBlockAt(x, placeY, z)
            if (!place.type.isAir) return@repeat

            return place.location
        }

        return null
    }

    private fun parseLootEntries(raw: Any?): List<LootEntry> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val typeName = anyToString(map["type"]) ?: anyToString(map["material"]) ?: return@mapNotNull null
            val material = Material.matchMaterial(typeName) ?: return@mapNotNull null

            val amount = anyToInt(map["amount"], -1)
            val minAmount = anyToInt(map["min_amount"], if (amount > 0) amount else 1).coerceAtLeast(1)
            val maxAmount = anyToInt(map["max_amount"], if (amount > 0) amount else minAmount).coerceAtLeast(1)
            val weight = anyToDouble(map["weight"], 1.0).coerceAtLeast(0.0)
            if (weight <= 0.0) return@mapNotNull null

            LootEntry(
                material = material,
                minAmount = minAmount,
                maxAmount = maxAmount.coerceAtLeast(minAmount),
                weight = weight,
            )
        }
    }

    private fun pickWeighted(entries: List<LootEntry>): LootEntry? {
        if (entries.isEmpty()) return null
        val total = entries.sumOf { it.weight }
        if (total <= 0.0) return null
        var r = Random.nextDouble() * total
        for (e in entries) {
            r -= e.weight
            if (r <= 0.0) return e
        }
        return entries.lastOrNull()
    }

    private fun placeInRandomEmptySlot(inv: Inventory, stack: ItemStack): Boolean {
        val size = inv.size
        if (size <= 0) return false

        val slots = (0 until size).shuffled(Random)
        for (slot in slots) {
            val existing = inv.getItem(slot)
            if (existing == null || existing.type == Material.AIR) {
                inv.setItem(slot, stack)
                return true
            }
        }
        return false
    }

    private fun setChestNameBestEffort(chest: Chest, name: String) {
        // Paper API: customName(Component) is always available
        chest.customName(Component.text(name))
    }
}
