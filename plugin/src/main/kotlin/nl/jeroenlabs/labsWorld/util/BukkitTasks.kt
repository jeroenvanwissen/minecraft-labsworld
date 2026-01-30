package nl.jeroenlabs.labsWorld.util

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

fun JavaPlugin.runSync(task: () -> Unit): BukkitTask =
    server.scheduler.runTask(this, Runnable { task() })

fun JavaPlugin.runAsync(task: () -> Unit): BukkitTask =
    server.scheduler.runTaskAsynchronously(this, Runnable { task() })
