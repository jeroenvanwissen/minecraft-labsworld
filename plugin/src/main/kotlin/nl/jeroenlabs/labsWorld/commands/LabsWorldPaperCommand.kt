package nl.jeroenlabs.labsWorld.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import nl.jeroenlabs.labsWorld.LabsWorld

class LabsWorldPaperCommand(
    plugin: LabsWorld,
) : BasicCommand {
    private val delegate = LabsWorldCommand(plugin)

    override fun execute(
        source: CommandSourceStack,
        args: Array<out String>,
    ) {
        delegate.handle(source.sender, "labsworld", args)
    }

    override fun permission(): String? = "labsworld.admin"
}
