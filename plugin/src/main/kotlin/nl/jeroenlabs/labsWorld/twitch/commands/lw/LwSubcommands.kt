package nl.jeroenlabs.labsWorld.twitch.commands.lw

/** All registered !lw subcommands */
object LwSubcommands {
    val all: List<LwSubcommand> = listOf(
        HelpSubcommand,
        SpawnSubcommand,
        DuelSubcommand,
        ReloadSubcommand,
    )
}
