package nl.jeroenlabs.labsWorld.twitch.actions

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import nl.jeroenlabs.labsWorld.util.anyToBool
import nl.jeroenlabs.labsWorld.util.anyToDouble
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString
import nl.jeroenlabs.labsWorld.util.anyToStringList
import nl.jeroenlabs.labsWorld.util.WorldStateUtils
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import kotlin.math.min
import kotlin.random.Random

object ActionExecutor {
    fun executeActions(context: TwitchContext, invocation: ActionInvocation, actions: List<ActionConfig>) {
        actions.forEach { action ->
            runCatching {
                executeAction(context, invocation, action)
            }.onFailure { err ->
                context.plugin.logger.warning(
                    "Action failed type='${action.type}' user='${invocation.userName}' error='${err.message}'",
                )
            }
        }
    }

    private fun executeAction(context: TwitchContext, invocation: ActionInvocation, action: ActionConfig) {
        when (action.type.lowercase()) {
            "npc.spawn" -> runSpawnNpc(context, invocation)
            "npc.swarm_player" -> runSwarmNpcsToPlayer(context, invocation, action.params)
            "npc.attack_player" -> runAttackPlayer(context, invocation, action.params)
            "player.fireworks" -> runFireworks(context, invocation, action.params)
            "player.heal" -> runHeal(context, invocation, action.params)
            "player.spawn_mob" -> runSpawnMob(context, invocation, action.params)
            "player.drop_items" -> runDropItems(context, invocation, action.params)
            "world.loot_chest", "player.loot_chest" -> runLootChest(context, invocation, action.params)
            "world.weather" -> runWeather(context, invocation, action.params)
            else -> error("Unknown action type '${action.type}'")
        }
    }

    private fun runAttackPlayer(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val plugin = context.plugin
        val target = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val durationSeconds = anyToInt(params["duration_seconds"], 30).coerceAtLeast(1)
        val heartsPerHit = anyToDouble(params["hearts_per_hit"], 2.0).coerceAtLeast(0.1)

        plugin.startAttackAllNpcs(target, durationSeconds, heartsPerHit)
            .onSuccess { count ->
                context.twitchClient.chat.sendMessage(
                    invocation.channelName,
                    if (count <= 0) "No Twitch NPCs found."
                    else "Sent $count NPC(s) to attack ${target.name} for ${durationSeconds}s (${heartsPerHit}❤/hit).",
                )
            }
            .onFailure { err ->
                error("NPC attack failed: ${err.message}")
            }
    }

    private fun runSwarmNpcsToPlayer(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val plugin = context.plugin
        val target = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val durationSeconds = anyToInt(params["duration_seconds"], 30).coerceAtLeast(1)
        plugin.startAggroAllNpcs(target, durationSeconds)
            .onSuccess { count ->
                context.twitchClient.chat.sendMessage(
                    invocation.channelName,
                    if (count <= 0) "No Twitch NPCs found." else "Sent $count NPC(s) after ${target.name} for ${durationSeconds}s.",
                )
            }
            .onFailure { err ->
                error("NPC swarm failed: ${err.message}")
            }
    }

    private data class LootEntry(
        val material: Material,
        val minAmount: Int,
        val maxAmount: Int,
        val weight: Double,
    )

    private fun runLootChest(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
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

        // 1) Apply name via BlockState.update (safe while chest is still empty)
        val state = block.state as? Chest ?: error("Placed chest but block state is not a chest")
        runCatching { setChestNameBestEffort(state, chestName) }
        state.update(true, false)

        // 2) Fill using the *live* tile-entity inventory. Do NOT call update after filling,
        // because some servers reset the inventory snapshot on update.
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

        // Fill next tick to ensure the tile entity is fully initialized.
        context.plugin.server.scheduler.runTask(context.plugin, fillRunner)

        if (announce) {
            context.twitchClient.chat.sendMessage(
                invocation.channelName,
                "A $chestName appeared near ${player.name}! (x=${block.x} y=${block.y} z=${block.z})",
            )
        }
    }

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
            // Hoppers can immediately pull items out of a chest placed above them.
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
        // Prefer Paper/Adventure API if present: customName(Component)
        val methods = chest.javaClass.methods
        methods.firstOrNull { it.name == "customName" && it.parameterCount == 1 }?.let { m ->
            val param = m.parameterTypes.firstOrNull()
            if (param != null && param.name == "net.kyori.adventure.text.Component") {
                m.invoke(chest, Component.text(name))
                return
            }
        }

        // Fallback: Spigot/Bukkit API setCustomName(String)
        methods.firstOrNull { it.name == "setCustomName" && it.parameterCount == 1 && it.parameterTypes[0] == String::class.java }?.let { m ->
            m.invoke(chest, name)
            return
        }
    }

    private fun runSpawnNpc(context: TwitchContext, invocation: ActionInvocation) {
        val plugin = context.plugin
        val spawnPoint = plugin.pickNpcSpawnPointSpawnLocation()
            ?: error("No NPC Spawn Point placed. Ask an admin to place one.")

        plugin.ensureNpcAtSpawnPoint(invocation.userId, invocation.userName, spawnPoint)
            .onSuccess { msg -> context.twitchClient.chat.sendMessage(invocation.channelName, msg) }
            .onFailure { err -> error("NPC spawn failed: ${err.message}") }
    }

    private fun runFireworks(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val count = anyToInt(params["count"], 1).coerceAtLeast(1)
        val power = anyToInt(params["power"], 1).coerceIn(0, 2)
        val shape = anyToString(params["shape"])?.lowercase() ?: "ball"
        val colors = ActionUtils.parseColors(anyToStringList(params["colors"]))

        repeat(count) {
            val location = player.location.clone().add(ActionUtils.randomOffset(0.6))
            val firework = player.world.spawn(location, Firework::class.java)
            val meta = firework.fireworkMeta
            val effect = FireworkEffect.builder()
                .with(ActionUtils.parseFireworkType(shape))
                .withColor(colors.ifEmpty { listOf(Color.WHITE) })
                .build()
            meta.power = power
            meta.addEffect(effect)
            firework.fireworkMeta = meta
        }
    }

    private fun runHeal(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val hearts = anyToDouble(params["hearts"], -1.0)
        val healthPoints = if (hearts >= 0) hearts * 2.0 else anyToDouble(params["health"], 4.0)
        if (healthPoints <= 0.0) return
        player.health = min(player.maxHealth, player.health + healthPoints)
    }

    private fun runSpawnMob(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val mobName = anyToString(params["mob"]) ?: error("Missing mob type")
        val entityType = ActionUtils.parseEntityType(mobName) ?: error("Unknown mob type '$mobName'")
        if (!entityType.isSpawnable || !entityType.isAlive) return

        val count = anyToInt(params["count"], 1).coerceAtLeast(1)
        val radius = anyToDouble(params["radius"], 2.0).coerceAtLeast(0.0)
        repeat(count) {
            val location = player.location.clone().add(ActionUtils.randomOffset(radius))
            player.world.spawnEntity(location, entityType)
        }
    }

    private fun runDropItems(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val itemsRaw = params["items"]
        val items = ActionUtils.parseItemStacks(itemsRaw)
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

    private fun runWeather(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params)
        val world = player?.world ?: ActionUtils.pickDefaultWorld(context.plugin.server.worlds) ?: return
        val state = anyToString(params["state"])?.lowercase() ?: "clear"
        val durationSeconds = anyToInt(params["duration_seconds"], 60).coerceAtLeast(1)
        val ticks = durationSeconds * 20

        WorldStateUtils.setWorldState(world, state, ticks)
    }
}
