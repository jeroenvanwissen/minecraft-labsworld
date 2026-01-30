# Minecraft Twitch City

This is a Minecraft mod that allows Twitch viewers to spawn NPC's in the game through chat commands.
It will be used on my Twitch stream to let viewers interact with my Minecraft world.
This Minecraft world will have a city with a big wall around it to keep the NPC's contained.
The NPC Spawn point will be in the middle of the city.

We will be building the Mod and the Minecraft world live on my Twitch stream.

## Phase 0

Clean up the mod code from previous experiments, make sure it compiles with latest dep versions.

## Phase 1

To start we will need to setup a basic Minecraft server with all required mods.
We need to build the basic mod that connects to Twitch and can handle channel point redemptions.
Implement simple NPC spawning.
We will build a simple Minecraft world with a city and a wall around it, with a spawn point in the middle.
We will need a custom block item that we can place in the world to mark the NPC spawn point.
## Phase 2

Implement some basic commands for the NPC's, like follow me, attack me, etc. ( linked to Twitch chat commands )

## Phase 3

Implement some sort of game mode where the NPC's do things..... ???

## TODO / BRAIN DUMP

* Twitch chat redeem to spawn viewer NPC's in Minecraft.
* NPC's should have viewer's name.
* A viewer can have only one NPC at a time.
* Twitch chat commands to interact with their NPC:
  * !attack - makes the NPC attack <ME>?
  * !follow - makes the NPC follow me around... / STALK ME!
  * ... things like that.


* !duel type game where one NPC initiates a duel with another NPC. Needs to accept the duel.
  * Random hits/miss until one dies. 10 hearts/hit points. Death NPC gets respawned as NONE.

* Capture the Flag mode....

* ADD DAMAGE TO NPC'S, we should be able to damage them, .. or have the TNT damage them ...
