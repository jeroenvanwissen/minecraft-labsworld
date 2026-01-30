package nl.jeroenlabs.labsWorld.npc

import nl.jeroenlabs.labsWorld.LabsWorld

internal fun startNpcDuel(
    plugin: LabsWorld,
    userAId: String,
    userAName: String,
    userBId: String,
    userBName: String,
    announce: (String) -> Unit,
): Result<Unit> =
    plugin.startNpcDuel(
        userAId = userAId,
        userAName = userAName,
        userBId = userBId,
        userBName = userBName,
        announce = announce,
    )
