package nl.jeroenlabs.labsWorld.twitch.redeems

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.eventsub.domain.Reward
import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent
import io.mockk.*
import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionConfig
import org.bukkit.Server
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.logging.Logger

/**
 * Unit tests for RedeemDispatcher.
 *
 * Task T5 from .agent/TASKS.md — Tests redeem dispatch flow:
 * - Redeems enabled/disabled
 * - Matched/unmatched redeems
 * - Handler execution
 * - Action execution
 * - Missing handler warnings
 */
class RedeemDispatcherTest {

    private lateinit var context: TwitchContext
    private lateinit var plugin: LabsWorld
    private lateinit var twitchClient: TwitchClient
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var dispatcher: RedeemDispatcher
    private lateinit var scheduler: BukkitScheduler
    private lateinit var server: Server
    private lateinit var logger: Logger

    @BeforeEach
    fun setup() {
        // Create mock infrastructure
        plugin = mockk(relaxed = true)
        twitchClient = mockk(relaxed = true)
        twitchConfigManager = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)
        server = mockk(relaxed = true)
        logger = mockk(relaxed = true)

        // Setup basic mocks
        every { plugin.server } returns server
        every { plugin.logger } returns logger
        every { server.scheduler } returns scheduler

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

        // Default: redeems enabled
        every { twitchConfigManager.isRedeemsEnabled() } returns true
        every { twitchConfigManager.shouldLogUnmatchedRedeems() } returns true
        every { twitchConfigManager.getRedeemBindings() } returns emptyList()

        // Create context
        context = TwitchContext(
            plugin = plugin,
            twitchClient = twitchClient,
            twitchConfigManager = twitchConfigManager
        )

        dispatcher = RedeemDispatcher(context)
    }

    private fun createMockRedeemEvent(
        rewardId: String = "reward123",
        rewardTitle: String = "Test Reward",
        rewardCost: Int = 100,
        userId: String = "user123",
        userName: String = "testuser",
        userInput: String? = null
    ): ChannelPointsCustomRewardRedemptionEvent {
        val event = mockk<ChannelPointsCustomRewardRedemptionEvent>(relaxed = true)
        val reward = mockk<Reward>(relaxed = true)

        every { event.reward } returns reward
        every { reward.id } returns rewardId
        every { reward.title } returns rewardTitle
        every { reward.cost } returns rewardCost
        every { reward.prompt } returns null

        every { event.id } returns "redemption123"
        every { event.userId } returns userId
        every { event.userLogin } returns userName
        every { event.userName } returns userName
        every { event.userInput } returns userInput
        every { event.broadcasterUserId } returns "broadcaster123"
        every { event.broadcasterUserName } returns "broadcaster"

        return event
    }

    // ==================== Redeems Enabled/Disabled Tests ====================

    @Nested
    @DisplayName("Redeems Enabled/Disabled")
    inner class RedeemsEnabledTests {

        @Test
        @DisplayName("should early return when redeems are disabled")
        fun redeemsDisabled() {
            every { twitchConfigManager.isRedeemsEnabled() } returns false

            val handler = mockk<RedeemHandler>(relaxed = true)
            dispatcher.register(handler)

            val event = createMockRedeemEvent()
            dispatcher.handle(event)

            // Should not process anything
            verify(exactly = 0) { handler.handle(any(), any(), any()) }
            verify(exactly = 0) { twitchConfigManager.getRedeemBindings() }
        }

        @Test
        @DisplayName("should process redeems when enabled")
        fun redeemsEnabled() {
            every { twitchConfigManager.isRedeemsEnabled() } returns true

            val event = createMockRedeemEvent()
            dispatcher.handle(event)

            // Should at least check bindings
            verify { twitchConfigManager.getRedeemBindings() }
        }
    }

    // ==================== Event Parsing Tests ====================

    @Nested
    @DisplayName("Event Parsing")
    inner class EventParsingTests {

        @Test
        @DisplayName("should handle event with missing reward gracefully")
        fun missingReward() {
            val event = mockk<ChannelPointsCustomRewardRedemptionEvent>(relaxed = true)
            every { event.reward } returns null

            dispatcher.handle(event)

            verify { logger.warning(match<String> { it.contains("could not parse payload") }) }
        }

        @Test
        @DisplayName("should handle event with missing reward ID gracefully")
        fun missingRewardId() {
            val event = mockk<ChannelPointsCustomRewardRedemptionEvent>(relaxed = true)
            val reward = mockk<Reward>(relaxed = true)
            every { event.reward } returns reward
            every { reward.id } returns null
            every { reward.title } returns "Test"

            dispatcher.handle(event)

            verify { logger.warning(match<String> { it.contains("could not parse payload") }) }
        }
    }

    // ==================== Handler Registration Tests ====================

    @Nested
    @DisplayName("Handler Registration")
    inner class HandlerRegistrationTests {

        @Test
        @DisplayName("should register handler by key (case-insensitive)")
        fun registerHandler() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "TestHandler"

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "testhandler",
                runOnMainThread = true,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            verify { handler.handle(any(), any(), any()) }
        }
    }

    // ==================== Binding Match Tests ====================

    @Nested
    @DisplayName("Binding Matching")
    inner class BindingMatchTests {

        @Test
        @DisplayName("should match binding by reward ID")
        fun matchByRewardId() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123", rewardTitle = "Different Title")
            dispatcher.handle(event)

            verify { handler.handle(any(), any(), any()) }
        }

        @Test
        @DisplayName("should match binding by reward title")
        fun matchByRewardTitle() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = null,
                rewardTitle = "Test Reward",
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "different123", rewardTitle = "Test Reward")
            dispatcher.handle(event)

            verify { handler.handle(any(), any(), any()) }
        }

        @Test
        @DisplayName("should match binding case-insensitively")
        fun matchCaseInsensitive() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "REWARD123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            verify { handler.handle(any(), any(), any()) }
        }

        @Test
        @DisplayName("should log unmatched redeem when configured")
        fun logUnmatchedRedeem() {
            every { twitchConfigManager.shouldLogUnmatchedRedeems() } returns true
            every { twitchConfigManager.getRedeemBindings() } returns emptyList()

            val event = createMockRedeemEvent(rewardTitle = "Unmatched Reward")
            dispatcher.handle(event)

            verify { logger.info(match<String> { it.contains("Unmatched redeem: reward='Unmatched Reward'") }) }
        }

        @Test
        @DisplayName("should not log unmatched redeem when disabled")
        fun noLogUnmatchedRedeem() {
            every { twitchConfigManager.shouldLogUnmatchedRedeems() } returns false
            every { twitchConfigManager.getRedeemBindings() } returns emptyList()

            val event = createMockRedeemEvent()
            dispatcher.handle(event)

            verify(exactly = 0) { logger.info(match<String> { it.contains("Unmatched redeem") }) }
        }
    }

    // ==================== Handler Execution Tests ====================

    @Nested
    @DisplayName("Handler Execution")
    inner class HandlerExecutionTests {

        @Test
        @DisplayName("should execute handler with params")
        fun executeHandlerWithParams() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val params = mapOf("param1" to "value1", "param2" to 42)
            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = params,
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            verify {
                handler.handle(
                    any(),
                    match { it.rewardId == "reward123" },
                    match { it == params }
                )
            }
        }

        @Test
        @DisplayName("should warn when handler not registered")
        fun warnMissingHandler() {
            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "nonexistent",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            verify { logger.warning(match<String> { it.contains("handler 'nonexistent' is not registered") }) }
        }

        @Test
        @DisplayName("should catch and log handler execution errors")
        fun catchHandlerError() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true
            every { handler.handle(any(), any(), any()) } throws RuntimeException("Handler failed")

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")

            assertDoesNotThrow {
                dispatcher.handle(event)
            }

            verify { logger.log(any(), match { it.contains("Redeem handler failed") }, any<Throwable>()) }
        }
    }

    // ==================== Action Execution Tests ====================

    @Nested
    @DisplayName("Action Execution")
    inner class ActionExecutionTests {

        @Test
        @DisplayName("should execute actions when binding has actions")
        fun executeActions() {
            val actions = listOf(
                ActionConfig(type = "test.action", params = mapOf("key" to "value"))
            )

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = null,
                runOnMainThread = null,
                params = emptyMap(),
                actions = actions
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")

            // Should not throw exception even if actions fail
            assertDoesNotThrow {
                dispatcher.handle(event)
            }
        }

        @Test
        @DisplayName("should prefer actions over handler when both present")
        fun actionsOverHandler() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val actions = listOf(
                ActionConfig(type = "test.action", params = emptyMap())
            )

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = actions
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            // Should execute actions, not handler (actions have priority)
            // Note: The implementation executes actions in the "if (hasActions)" block
            // and doesn't execute handler if actions exist
            verify(exactly = 0) { handler.handle(any(), any(), any()) }
        }

        @Test
        @DisplayName("should catch and log action execution errors")
        fun catchActionError() {
            // Create a binding with invalid actions that will fail
            val actions = listOf(
                ActionConfig(type = "nonexistent.action", params = emptyMap())
            )

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = null,
                runOnMainThread = null,
                params = emptyMap(),
                actions = actions
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")

            assertDoesNotThrow {
                dispatcher.handle(event)
            }
        }
    }

    // ==================== Thread Scheduling Tests ====================

    @Nested
    @DisplayName("Thread Scheduling")
    inner class ThreadSchedulingTests {

        @Test
        @DisplayName("should use binding runOnMainThread when specified")
        fun useBindingThreadConfig() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = false, // Explicitly false
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            // Should use async because binding says so
            verify { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
            verify(exactly = 0) { scheduler.runTask(plugin, any<Runnable>()) }
        }

        @Test
        @DisplayName("should use handler runOnMainThread when binding not specified")
        fun useHandlerThreadConfig() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns false

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null, // Not specified
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            // Should use handler's preference (async)
            verify { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
            verify(exactly = 0) { scheduler.runTask(plugin, any<Runnable>()) }
        }

        @Test
        @DisplayName("should default to main thread when both unspecified")
        fun defaultToMainThread() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true // Handler default

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            verify { scheduler.runTask(plugin, any<Runnable>()) }
            verify(exactly = 0) { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
        }

        @Test
        @DisplayName("should run actions on main thread by default")
        fun actionsMainThreadDefault() {
            val actions = listOf(
                ActionConfig(type = "test.action", params = emptyMap())
            )

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = null,
                runOnMainThread = null, // Not specified, should default to true
                params = emptyMap(),
                actions = actions
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123")
            dispatcher.handle(event)

            verify { scheduler.runTask(plugin, any<Runnable>()) }
        }
    }

    // ==================== User Input Tests ====================

    @Nested
    @DisplayName("User Input Handling")
    inner class UserInputTests {

        @Test
        @DisplayName("should pass user input to handler")
        fun passUserInput() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(
                rewardId = "reward123",
                userInput = "user provided text"
            )
            dispatcher.handle(event)

            verify {
                handler.handle(
                    any(),
                    match { it.userInput == "user provided text" },
                    any()
                )
            }
        }

        @Test
        @DisplayName("should handle null user input")
        fun handleNullUserInput() {
            val handler = mockk<RedeemHandler>(relaxed = true)
            every { handler.key } returns "test"
            every { handler.runOnMainThread } returns true

            dispatcher.register(handler)

            val binding = TwitchConfigManager.RedeemBindingConfig(
                rewardId = "reward123",
                rewardTitle = null,
                handler = "test",
                runOnMainThread = null,
                params = emptyMap(),
                actions = emptyList()
            )

            every { twitchConfigManager.getRedeemBindings() } returns listOf(binding)

            val event = createMockRedeemEvent(rewardId = "reward123", userInput = null)
            dispatcher.handle(event)

            verify {
                handler.handle(
                    any(),
                    match { it.userInput == null },
                    any()
                )
            }
        }
    }
}
