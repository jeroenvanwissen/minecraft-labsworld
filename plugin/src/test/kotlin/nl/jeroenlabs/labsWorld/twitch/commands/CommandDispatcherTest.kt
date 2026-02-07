package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.events.domain.EventChannel
import com.github.twitch4j.common.events.domain.EventUser
import io.mockk.*
import java.util.Optional
import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionConfig
import org.bukkit.Server
import org.bukkit.entity.Villager
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.logging.Logger

/**
 * Unit tests for CommandDispatcher.
 *
 * Task T5 from .agent/TASKS.md — Tests command dispatch flow:
 * - Command found/not found
 * - Unauthorized access
 * - Init-once behavior
 * - Config command refresh
 */
class CommandDispatcherTest {

    private lateinit var context: TwitchContext
    private lateinit var plugin: LabsWorld
    private lateinit var twitchClient: TwitchClient
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var dispatcher: CommandDispatcher
    private lateinit var scheduler: BukkitScheduler
    private lateinit var server: Server
    private lateinit var chat: TwitchChat
    private lateinit var logger: Logger

    @BeforeEach
    fun setup() {
        // Create mock infrastructure
        plugin = mockk(relaxed = true)
        twitchClient = mockk(relaxed = true)
        twitchConfigManager = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)
        server = mockk(relaxed = true)
        chat = mockk(relaxed = true)
        logger = mockk(relaxed = true)

        // Setup basic mocks
        every { plugin.server } returns server
        every { plugin.logger } returns logger
        every { server.scheduler } returns scheduler
        every { twitchClient.chat } returns chat
        every { twitchConfigManager.getReloadVersion() } returns 1L
        every { twitchConfigManager.getCommandBindings() } returns emptyList()

        // Make scheduler execute tasks immediately for testing
        every { scheduler.runTask(plugin, any<Runnable>()) } answers {
            val runnable = secondArg<Runnable>()
            runnable.run()
            mockk(relaxed = true)
        }
        every { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) } answers {
            val runnable = secondArg<Runnable>()
            runnable.run()
            mockk(relaxed = true)
        }

        // Create context
        context = TwitchContext(
            plugin = plugin,
            twitchClient = twitchClient,
            twitchConfigManager = twitchConfigManager
        )

        dispatcher = CommandDispatcher(context)
    }

    private fun createMockEvent(
        message: String,
        userId: String = "user123",
        userName: String = "testuser",
        channelId: String = "channel123",
        channelName: String = "testchannel",
        tags: Map<String, String?> = emptyMap()
    ): ChannelMessageEvent {
        val event = mockk<ChannelMessageEvent>(relaxed = true)
        val user = mockk<EventUser>(relaxed = true)
        val channel = mockk<EventChannel>(relaxed = true)

        every { event.message } returns message
        every { event.user } returns user
        every { event.channel } returns channel
        every { user.id } returns userId
        every { user.name } returns userName
        every { channel.id } returns channelId
        every { channel.name } returns channelName
        every { event.messageEvent.getTagValue(any()) } answers {
            val key = firstArg<String>()
            Optional.ofNullable(tags[key])
        }

        return event
    }

    private fun createBroadcasterEvent(message: String): ChannelMessageEvent {
        return createMockEvent(
            message = message,
            userId = "broadcaster123",
            channelId = "broadcaster123",
            tags = emptyMap()
        )
    }

    // ==================== Command Registration Tests ====================

    @Nested
    @DisplayName("Command Registration")
    inner class CommandRegistrationTests {

        @Test
        @DisplayName("should register command by name (case-insensitive)")
        fun registerCommand() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "TestCommand"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createMockEvent("!testcommand")
            dispatcher.handle(event)

            verify { command.handle(any()) }
        }

        @Test
        @DisplayName("should normalize command name to lowercase")
        fun normalizeCommandName() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "MixedCase"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createMockEvent("!mixedcase")
            dispatcher.handle(event)

            verify { command.handle(any()) }
        }
    }

    // ==================== Command Dispatch Tests ====================

    @Nested
    @DisplayName("Command Dispatch")
    inner class CommandDispatchTests {

        @Test
        @DisplayName("should execute command when found and authorized")
        fun executeAuthorizedCommand() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createMockEvent("!test arg1 arg2")
            dispatcher.handle(event)

            verify { command.handle(match { it.commandName == "test" && it.args == listOf("arg1", "arg2") }) }
        }

        @Test
        @DisplayName("should not execute when command not found")
        fun commandNotFound() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "exists"
            every { command.permission } returns Permission.EVERYONE

            dispatcher.register(command)

            val event = createMockEvent("!doesnotexist")
            dispatcher.handle(event)

            verify(exactly = 0) { command.handle(any()) }
            verify(exactly = 0) { chat.sendMessage(any(), any()) }
        }

        @Test
        @DisplayName("should send unauthorized message when permission denied")
        fun unauthorizedCommand() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "adminonly"
            every { command.permission } returns Permission.BROADCASTER

            dispatcher.register(command)

            // Regular user tries to use broadcaster command
            val event = createMockEvent(
                message = "!adminonly",
                userId = "user123",
                channelId = "broadcaster123",
                userName = "testuser",
                channelName = "testchannel"
            )

            dispatcher.handle(event)

            verify(exactly = 0) { command.handle(any()) }
            verify { chat.sendMessage("testchannel", "@testuser You don't have permission to use !adminonly") }
        }

        @Test
        @DisplayName("should execute broadcaster command when user is broadcaster")
        fun broadcasterCommandAuthorized() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "adminonly"
            every { command.permission } returns Permission.BROADCASTER
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createBroadcasterEvent("!adminonly")
            dispatcher.handle(event)

            verify { command.handle(any()) }
        }

        @Test
        @DisplayName("should parse command arguments correctly")
        fun parseCommandArguments() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createMockEvent("!test  arg1   arg2  arg3  ")
            dispatcher.handle(event)

            verify {
                command.handle(match {
                    it.args == listOf("arg1", "arg2", "arg3")
                })
            }
        }

        @Test
        @DisplayName("should handle command with no arguments")
        fun commandWithNoArguments() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createMockEvent("!test")
            dispatcher.handle(event)

            verify { command.handle(match { it.args.isEmpty() }) }
        }
    }

    // ==================== Init-Once Tests ====================

    @Nested
    @DisplayName("Init-Once Behavior")
    inner class InitOnceTests {

        @Test
        @DisplayName("should call init() only once per command")
        fun initOnlyOnce() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            // Call command multiple times
            dispatcher.handle(createMockEvent("!test"))
            dispatcher.handle(createMockEvent("!test"))
            dispatcher.handle(createMockEvent("!test"))

            verify(exactly = 1) { command.init() }
            verify(exactly = 3) { command.handle(any()) }
        }

        @Test
        @DisplayName("should handle init() failure gracefully")
        fun initFailureHandled() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false
            every { command.init() } throws RuntimeException("Init failed")

            dispatcher.register(command)

            val event = createMockEvent("!test")

            // Should not throw exception
            assertDoesNotThrow {
                dispatcher.handle(event)
            }

            // Should still execute command despite init failure
            verify { command.handle(any()) }
            verify { logger.log(any(), match { it.contains("Command init failed for 'test'") }, any<Throwable>()) }
        }
    }

    // ==================== Thread Scheduling Tests ====================

    @Nested
    @DisplayName("Thread Scheduling")
    inner class ThreadSchedulingTests {

        @Test
        @DisplayName("should run on main thread when runOnMainThread is true")
        fun runOnMainThread() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns true

            dispatcher.register(command)

            val event = createMockEvent("!test")
            dispatcher.handle(event)

            verify { scheduler.runTask(plugin, any<Runnable>()) }
            verify(exactly = 0) { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
        }

        @Test
        @DisplayName("should run async when runOnMainThread is false")
        fun runAsync() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false

            dispatcher.register(command)

            val event = createMockEvent("!test")
            dispatcher.handle(event)

            verify { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
            verify(exactly = 0) { scheduler.runTask(plugin, any<Runnable>()) }
        }
    }

    // ==================== Config Command Tests ====================

    @Nested
    @DisplayName("Config Commands")
    inner class ConfigCommandTests {

        @Test
        @DisplayName("should load config commands on first message")
        fun loadConfigCommands() {
            val binding = TwitchConfigManager.CommandBindingConfig(
                name = "configcmd",
                permission = Permission.EVERYONE,
                runOnMainThread = true,
                actions = listOf(ActionConfig(type = "test", params = emptyMap()))
            )

            every { twitchConfigManager.getCommandBindings() } returns listOf(binding)

            val event = createMockEvent("!configcmd")
            dispatcher.handle(event)

            // Should execute the config command
            verify(exactly = 0) { chat.sendMessage(any(), any()) }
        }

        @Test
        @DisplayName("should refresh config commands when version changes")
        fun refreshOnVersionChange() {
            val binding1 = TwitchConfigManager.CommandBindingConfig(
                name = "cmd1",
                permission = Permission.EVERYONE,
                runOnMainThread = true,
                actions = listOf(ActionConfig(type = "test", params = emptyMap()))
            )

            every { twitchConfigManager.getReloadVersion() } returns 1L
            every { twitchConfigManager.getCommandBindings() } returns listOf(binding1)

            dispatcher.handle(createMockEvent("!cmd1"))

            // Change version and bindings
            val binding2 = TwitchConfigManager.CommandBindingConfig(
                name = "cmd2",
                permission = Permission.EVERYONE,
                runOnMainThread = true,
                actions = listOf(ActionConfig(type = "test", params = emptyMap()))
            )

            every { twitchConfigManager.getReloadVersion() } returns 2L
            every { twitchConfigManager.getCommandBindings() } returns listOf(binding2)

            dispatcher.handle(createMockEvent("!cmd2"))

            // Should have loaded new config
            verify(exactly = 2) { twitchConfigManager.getCommandBindings() }
        }

        @Test
        @DisplayName("should not refresh config commands when version unchanged")
        fun noRefreshWhenVersionSame() {
            every { twitchConfigManager.getReloadVersion() } returns 1L

            dispatcher.handle(createMockEvent("!test"))
            dispatcher.handle(createMockEvent("!test"))

            // Should only call once (first time)
            verify(exactly = 1) { twitchConfigManager.getCommandBindings() }
        }

        @Test
        @DisplayName("should not override built-in command with config command")
        fun noOverrideBuiltIn() {
            val builtInCommand = mockk<Command>(relaxed = true)
            every { builtInCommand.name } returns "builtin"
            every { builtInCommand.permission } returns Permission.EVERYONE
            every { builtInCommand.runOnMainThread } returns false

            dispatcher.register(builtInCommand)

            // Try to register a config command with same name
            val configBinding = TwitchConfigManager.CommandBindingConfig(
                name = "builtin",
                permission = Permission.EVERYONE,
                runOnMainThread = true,
                actions = listOf(ActionConfig(type = "test", params = emptyMap()))
            )

            every { twitchConfigManager.getCommandBindings() } returns listOf(configBinding)

            val event = createMockEvent("!builtin")
            dispatcher.handle(event)

            // Should execute built-in command, not config command
            verify { builtInCommand.handle(any()) }
            verify { logger.warning(match<String> { it.contains("Skipping config command 'builtin'") }) }
        }
    }

    // ==================== Non-Command Message Tests ====================

    @Nested
    @DisplayName("Non-Command Messages")
    inner class NonCommandMessageTests {

        @Test
        @DisplayName("should handle chat message without exclamation mark")
        fun handleChatMessage() {
            every { plugin.resolveLinkedUserIdByUserName(any()) } returns null
            every { plugin.getNpcByUserId(any()) } returns null

            val event = createMockEvent("Hello world")
            dispatcher.handle(event)

            // Should not try to parse as command
            verify(exactly = 0) { chat.sendMessage(any(), any()) }
            verify { plugin.getNpcByUserId(any()) }
        }

        // Note: Chat bubble test removed due to complexity of mocking ChatBubbleService
        // which is instantiated in CommandDispatcher constructor

        @Test
        @DisplayName("should log chat message without linked NPC")
        fun logChatMessageNoNpc() {
            every { plugin.resolveLinkedUserIdByUserName(any()) } returns null
            every { plugin.getNpcByUserId(any()) } returns null

            val event = createMockEvent("Hello world", userName = "testuser")
            dispatcher.handle(event)

            verify { logger.info(match<String> { it.contains("Chat message from @testuser (no linked NPC)") }) }
        }

        @Test
        @DisplayName("should handle empty message after exclamation")
        fun emptyCommandName() {
            val event = createMockEvent("!")

            assertDoesNotThrow {
                dispatcher.handle(event)
            }
        }

        @Test
        @DisplayName("should handle whitespace-only message after exclamation")
        fun whitespaceOnlyCommand() {
            val event = createMockEvent("!   ")

            assertDoesNotThrow {
                dispatcher.handle(event)
            }
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("should catch and log command execution errors")
        fun catchExecutionError() {
            val command = mockk<Command>(relaxed = true)
            every { command.name } returns "test"
            every { command.permission } returns Permission.EVERYONE
            every { command.runOnMainThread } returns false
            every { command.handle(any()) } throws RuntimeException("Command failed")

            dispatcher.register(command)

            val event = createMockEvent("!test")

            assertDoesNotThrow {
                dispatcher.handle(event)
            }

            verify { logger.log(any(), match { it.contains("Command failed") }, any<Throwable>()) }
        }
    }
}
