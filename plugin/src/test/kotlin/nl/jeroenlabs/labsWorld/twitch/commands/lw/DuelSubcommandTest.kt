package nl.jeroenlabs.labsWorld.twitch.commands.lw

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.events.domain.EventChannel
import com.github.twitch4j.common.events.domain.EventUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation
import nl.jeroenlabs.labsWorld.twitch.commands.Permission
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional

class DuelSubcommandTest {

    private lateinit var plugin: LabsWorld
    private lateinit var twitchClient: TwitchClient
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var context: TwitchContext
    private lateinit var chat: TwitchChat

    private val defaultDuelConfig = TwitchConfigManager.DuelConfig(
        hitChance = 0.65,
        speed = 1.15,
        attackRange = 1.9,
        maxHp = 10,
        respawnDelaySeconds = 10L,
        permission = Permission.SUBSCRIBER,
    )

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        twitchClient = mockk(relaxed = true)
        twitchConfigManager = mockk(relaxed = true)
        chat = mockk(relaxed = true)

        every { twitchClient.chat } returns chat
        every { twitchConfigManager.getDuelConfig() } returns defaultDuelConfig

        context = TwitchContext(
            plugin = plugin,
            twitchClient = twitchClient,
            twitchConfigManager = twitchConfigManager,
        )
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }

    private fun createMockEvent(
        userId: String = "user123",
        userName: String = "testuser",
        channelId: String = "channel123",
        channelName: String = "testchannel",
        tags: Map<String, String?> = emptyMap(),
    ): ChannelMessageEvent {
        val event = mockk<ChannelMessageEvent>(relaxed = true)
        val user = mockk<EventUser>(relaxed = true)
        val channel = mockk<EventChannel>(relaxed = true)

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

    private fun createInvocation(
        event: ChannelMessageEvent,
        args: List<String> = listOf("duel"),
    ): CommandInvocation = CommandInvocation(
        context = context,
        event = event,
        commandName = "lw",
        args = args,
    )

    // ==================== Permission Check ====================

    @Nested
    @DisplayName("Permission Check")
    inner class PermissionCheckTests {

        @Test
        @DisplayName("should reject viewer without subscriber badge")
        fun rejectsNonSubscriber() {
            val event = createMockEvent(tags = emptyMap())
            val inv = createInvocation(event, listOf("duel", "@opponent"))

            DuelSubcommand.handle(context, inv)

            verify { chat.sendMessage("testchannel", match<String> { it.contains("subscriber") }) }
        }

        @Test
        @DisplayName("should allow subscriber")
        fun allowsSubscriber() {
            val event = createMockEvent(tags = mapOf("subscriber" to "1"))
            val inv = createInvocation(event, listOf("duel", "@opponent"))

            every { plugin.getStoredLinkedUserName("user123") } returns "testuser"

            DuelSubcommand.handle(context, inv)

            // Should not get the permission error — will proceed to target resolution
            verify(exactly = 0) { chat.sendMessage(any(), match<String> { it.contains("subscriber") }) }
        }

        @Test
        @DisplayName("should allow broadcaster regardless of configured permission")
        fun allowsBroadcaster() {
            val event = createMockEvent(
                userId = "channel123",
                channelId = "channel123",
            )
            val inv = createInvocation(event, listOf("duel", "@opponent"))

            every { plugin.getStoredLinkedUserName("channel123") } returns "broadcaster"

            DuelSubcommand.handle(context, inv)

            verify(exactly = 0) { chat.sendMessage(any(), match<String> { it.contains("subscriber") }) }
        }

        @Test
        @DisplayName("should allow moderator when permission is subscriber")
        fun allowsModeratorWhenSubscriberRequired() {
            val event = createMockEvent(tags = mapOf("mod" to "1"))
            val inv = createInvocation(event, listOf("duel", "@opponent"))

            every { plugin.getStoredLinkedUserName("user123") } returns "testuser"

            DuelSubcommand.handle(context, inv)

            verify(exactly = 0) { chat.sendMessage(any(), match<String> { it.contains("subscriber") }) }
        }

        @Test
        @DisplayName("should use configurable permission level")
        fun usesConfigurablePermission() {
            every { twitchConfigManager.getDuelConfig() } returns defaultDuelConfig.copy(
                permission = Permission.MODERATOR,
            )

            // Subscriber should be rejected when permission is MODERATOR
            val event = createMockEvent(tags = mapOf("subscriber" to "1"))
            val inv = createInvocation(event, listOf("duel", "@opponent"))

            DuelSubcommand.handle(context, inv)

            verify { chat.sendMessage("testchannel", match<String> { it.contains("moderator") }) }
        }

        @Test
        @DisplayName("should allow everyone when permission is EVERYONE")
        fun allowsEveryoneWhenConfigured() {
            every { twitchConfigManager.getDuelConfig() } returns defaultDuelConfig.copy(
                permission = Permission.EVERYONE,
            )

            val event = createMockEvent(tags = emptyMap())
            val inv = createInvocation(event, listOf("duel", "@opponent"))

            every { plugin.getStoredLinkedUserName("user123") } returns "testuser"

            DuelSubcommand.handle(context, inv)

            // Should not get the permission error
            verify(exactly = 0) { chat.sendMessage(any(), match<String> { it.contains("need to be") }) }
        }
    }
}
