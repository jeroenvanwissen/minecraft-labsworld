package nl.jeroenlabs.labsWorld.twitch.commands

class CommandRegistry(
    val context: CommandContext,
) {
    private val commands: List<Command<*>> by lazy {
        listOf(
            LwCommand(context),
            // Add more commands here
        )
    }

    fun getCommand(name: String): Command<*>? = commands.find { it.name.equals(name, ignoreCase = true) }

    fun getAllCommands(): List<Command<*>> = commands
}
