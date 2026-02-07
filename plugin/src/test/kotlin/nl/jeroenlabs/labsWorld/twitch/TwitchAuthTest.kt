package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.events.domain.EventChannel
import com.github.twitch4j.common.events.domain.EventUser
import io.mockk.every
import io.mockk.mockk
import nl.jeroenlabs.labsWorld.twitch.commands.Permission
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.Optional

/**
 * Permission matrix unit tests for TwitchAuth.
 *
 * Task T4 from .agent/TASKS.md — Codifies permission behavior for all roles
 * with table-style tests to prevent regression around role precedence.
 */
class TwitchAuthTest {

    // ==================== Test Fixtures ====================

    /**
     * Creates a mock ChannelMessageEvent with specified user role and badges.
     */
    private fun createMockEvent(
        userId: String = "user123",
        channelId: String = "channel123",
        tags: Map<String, String?> = emptyMap()
    ): ChannelMessageEvent {
        val event = mockk<ChannelMessageEvent>(relaxed = true)
        val user = mockk<EventUser>(relaxed = true)
        val channel = mockk<EventChannel>(relaxed = true)

        every { user.id } returns userId
        every { channel.id } returns channelId
        every { event.user } returns user
        every { event.channel } returns channel

        // Mock messageEvent.getTagValue() for each provided tag
        every { event.messageEvent.getTagValue(any()) } answers {
            val key = firstArg<String>()
            Optional.ofNullable(tags[key])
        }

        return event
    }

    /**
     * Creates a broadcaster event (user ID matches channel ID).
     */
    private fun createBroadcasterEvent(): ChannelMessageEvent {
        return createMockEvent(
            userId = "broadcaster123",
            channelId = "broadcaster123",
            tags = emptyMap()
        )
    }

    /**
     * Creates a moderator event.
     */
    private fun createModeratorEvent(): ChannelMessageEvent {
        return createMockEvent(
            userId = "mod456",
            channelId = "broadcaster123",
            tags = mapOf(
                "mod" to "1",
                "badges" to "moderator/1"
            )
        )
    }

    /**
     * Creates a VIP event.
     */
    private fun createVipEvent(): ChannelMessageEvent {
        return createMockEvent(
            userId = "vip789",
            channelId = "broadcaster123",
            tags = mapOf(
                "vip" to "1",
                "badges" to "vip/1"
            )
        )
    }

    /**
     * Creates a subscriber event.
     */
    private fun createSubscriberEvent(): ChannelMessageEvent {
        return createMockEvent(
            userId = "sub999",
            channelId = "broadcaster123",
            tags = mapOf(
                "subscriber" to "1",
                "badges" to "subscriber/12"
            )
        )
    }

    /**
     * Creates a regular viewer event (no special roles).
     */
    private fun createRegularViewerEvent(): ChannelMessageEvent {
        return createMockEvent(
            userId = "viewer001",
            channelId = "broadcaster123",
            tags = emptyMap()
        )
    }

    // ==================== Role Detection Tests ====================

    @Nested
    @DisplayName("Role Detection")
    inner class RoleDetectionTests {

        @Test
        @DisplayName("should detect broadcaster when user ID matches channel ID")
        fun detectBroadcaster() {
            val event = createBroadcasterEvent()
            assertTrue(TwitchAuth.isBroadcaster(event))
        }

        @Test
        @DisplayName("should not detect broadcaster when IDs don't match")
        fun notBroadcaster() {
            val event = createModeratorEvent()
            assertFalse(TwitchAuth.isBroadcaster(event))
        }

        @Test
        @DisplayName("should detect moderator from mod tag")
        fun detectModFromTag() {
            val event = createMockEvent(
                tags = mapOf("mod" to "1")
            )
            assertTrue(TwitchAuth.isModerator(event))
        }

        @Test
        @DisplayName("should detect moderator from badges")
        fun detectModFromBadges() {
            val event = createMockEvent(
                tags = mapOf("badges" to "moderator/1,subscriber/6")
            )
            assertTrue(TwitchAuth.isModerator(event))
        }

        @Test
        @DisplayName("should not detect moderator for regular viewer")
        fun notModerator() {
            val event = createRegularViewerEvent()
            assertFalse(TwitchAuth.isModerator(event))
        }

        @Test
        @DisplayName("should detect broadcaster or moderator")
        fun broadcasterOrModerator() {
            assertTrue(TwitchAuth.isBroadcasterOrModerator(createBroadcasterEvent()))
            assertTrue(TwitchAuth.isBroadcasterOrModerator(createModeratorEvent()))
            assertFalse(TwitchAuth.isBroadcasterOrModerator(createVipEvent()))
        }
    }

    // ==================== Tag Value Extraction Tests ====================

    @Nested
    @DisplayName("Tag Value Extraction")
    inner class TagValueExtractionTests {

        @Test
        @DisplayName("should extract individual tag values from event")
        fun extractTagValues() {
            val event = createMockEvent(
                tags = mapOf(
                    "mod" to "1",
                    "subscriber" to "1",
                    "color" to "#FF0000"
                )
            )
            assertEquals("1", TwitchAuth.getTagValue(event, "mod"))
            assertEquals("1", TwitchAuth.getTagValue(event, "subscriber"))
            assertEquals("#FF0000", TwitchAuth.getTagValue(event, "color"))
        }

        @Test
        @DisplayName("should return null when tag access throws")
        fun tagAccessThrows() {
            val event = mockk<ChannelMessageEvent>(relaxed = true)
            every { event.messageEvent.getTagValue(any()) } throws Exception("No tags")

            assertNull(TwitchAuth.getTagValue(event, "mod"))
        }

        @Test
        @DisplayName("should return null for missing tags")
        fun missingTagValues() {
            val event = createMockEvent(tags = mapOf("key1" to "value1"))
            assertEquals("value1", TwitchAuth.getTagValue(event, "key1"))
            assertNull(TwitchAuth.getTagValue(event, "nonexistent"))
        }
    }

    // ==================== Permission Matrix Tests ====================

    @Nested
    @DisplayName("Permission Matrix")
    inner class PermissionMatrixTests {

        /**
         * Permission matrix test using parameterized tests for comprehensive coverage.
         *
         * Matrix:
         * | Permission   | Broadcaster | Moderator | VIP   | Subscriber | Regular |
         * |--------------|-------------|-----------|-------|------------|---------|
         * | BROADCASTER  | ✓           | ✗         | ✗     | ✗          | ✗       |
         * | MODERATOR    | ✓           | ✓         | ✗     | ✗          | ✗       |
         * | VIP          | ✓           | ✓         | ✓     | ✗          | ✗       |
         * | SUBSCRIBER   | ✓           | ✓         | ✓     | ✓          | ✗       |
         * | EVERYONE     | ✓           | ✓         | ✓     | ✓          | ✓       |
         */
        @ParameterizedTest(name = "{0} permission for {1} should be {2}")
        @CsvSource(
            // BROADCASTER permission
            "BROADCASTER, BROADCASTER, true",
            "BROADCASTER, MODERATOR, false",
            "BROADCASTER, VIP, false",
            "BROADCASTER, SUBSCRIBER, false",
            "BROADCASTER, REGULAR, false",

            // MODERATOR permission
            "MODERATOR, BROADCASTER, true",
            "MODERATOR, MODERATOR, true",
            "MODERATOR, VIP, false",
            "MODERATOR, SUBSCRIBER, false",
            "MODERATOR, REGULAR, false",

            // VIP permission
            "VIP, BROADCASTER, true",
            "VIP, MODERATOR, true",
            "VIP, VIP, true",
            "VIP, SUBSCRIBER, false",
            "VIP, REGULAR, false",

            // SUBSCRIBER permission
            "SUBSCRIBER, BROADCASTER, true",
            "SUBSCRIBER, MODERATOR, true",
            "SUBSCRIBER, VIP, true",
            "SUBSCRIBER, SUBSCRIBER, true",
            "SUBSCRIBER, REGULAR, false",

            // EVERYONE permission
            "EVERYONE, BROADCASTER, true",
            "EVERYONE, MODERATOR, true",
            "EVERYONE, VIP, true",
            "EVERYONE, SUBSCRIBER, true",
            "EVERYONE, REGULAR, true"
        )
        fun permissionMatrix(
            permissionName: String,
            roleName: String,
            expectedAuthorized: Boolean
        ) {
            val permission = Permission.valueOf(permissionName)
            val event = when (roleName) {
                "BROADCASTER" -> createBroadcasterEvent()
                "MODERATOR" -> createModeratorEvent()
                "VIP" -> createVipEvent()
                "SUBSCRIBER" -> createSubscriberEvent()
                "REGULAR" -> createRegularViewerEvent()
                else -> throw IllegalArgumentException("Unknown role: $roleName")
            }

            val result = TwitchAuth.isAuthorized(permission, event)
            assertEquals(
                expectedAuthorized,
                result,
                "$roleName should ${if (expectedAuthorized) "pass" else "fail"} $permissionName permission"
            )
        }

        @Test
        @DisplayName("EVERYONE permission should always pass")
        fun everyoneAlwaysPasses() {
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, createBroadcasterEvent()))
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, createModeratorEvent()))
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, createVipEvent()))
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, createSubscriberEvent()))
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, createRegularViewerEvent()))
        }

        @Test
        @DisplayName("Broadcaster should always pass except BROADCASTER-only permission for non-broadcasters")
        fun broadcasterAlwaysPasses() {
            val event = createBroadcasterEvent()
            assertTrue(TwitchAuth.isAuthorized(Permission.BROADCASTER, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, event))
        }
    }

    // ==================== Role Precedence Tests ====================

    @Nested
    @DisplayName("Role Precedence")
    inner class RolePrecedenceTests {

        @Test
        @DisplayName("Moderator should inherit VIP and SUBSCRIBER permissions")
        fun moderatorInheritance() {
            val event = createModeratorEvent()
            assertTrue(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.BROADCASTER, event))
        }

        @Test
        @DisplayName("VIP should inherit SUBSCRIBER permission")
        fun vipInheritance() {
            val event = createVipEvent()
            assertTrue(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.BROADCASTER, event))
        }

        @Test
        @DisplayName("Subscriber should not inherit higher permissions")
        fun subscriberNoInheritance() {
            val event = createSubscriberEvent()
            assertTrue(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.BROADCASTER, event))
        }

        @Test
        @DisplayName("Regular viewer should only pass EVERYONE permission")
        fun regularViewerOnlyEveryone() {
            val event = createRegularViewerEvent()
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.BROADCASTER, event))
        }
    }

    // ==================== Badge Detection Tests ====================

    @Nested
    @DisplayName("Badge Detection")
    inner class BadgeDetectionTests {

        @Test
        @DisplayName("should detect moderator from badge string")
        fun moderatorBadge() {
            val event = createMockEvent(
                tags = mapOf("badges" to "moderator/1")
            )
            assertTrue(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
        }

        @Test
        @DisplayName("should detect VIP from badge string")
        fun vipBadge() {
            val event = createMockEvent(
                tags = mapOf("badges" to "vip/1")
            )
            assertTrue(TwitchAuth.isAuthorized(Permission.VIP, event))
        }

        @Test
        @DisplayName("should detect subscriber from badge string")
        fun subscriberBadge() {
            val event = createMockEvent(
                tags = mapOf("badges" to "subscriber/12")
            )
            assertTrue(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
        }

        @Test
        @DisplayName("should handle multiple badges in badge string")
        fun multipleBadges() {
            val event = createMockEvent(
                tags = mapOf("badges" to "subscriber/24,bits/1000,moderator/1")
            )
            assertTrue(TwitchAuth.isModerator(event))
            assertTrue(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
        }

        @Test
        @DisplayName("should handle empty badges string")
        fun emptyBadges() {
            val event = createMockEvent(
                tags = mapOf("badges" to "")
            )
            assertFalse(TwitchAuth.isModerator(event))
            assertFalse(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
        }

        @Test
        @DisplayName("should prefer tag over badge for role detection")
        fun tagOverBadge() {
            val event = createMockEvent(
                tags = mapOf(
                    "mod" to "1",
                    "badges" to ""
                )
            )
            assertTrue(TwitchAuth.isModerator(event))
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("should handle missing tags gracefully")
        fun missingTags() {
            val event = createMockEvent(tags = emptyMap())
            assertFalse(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.EVERYONE, event))
        }

        @Test
        @DisplayName("should handle malformed tag values")
        fun malformedTags() {
            val event = createMockEvent(
                tags = mapOf(
                    "mod" to "invalid",
                    "vip" to "maybe",
                    "subscriber" to "yes"
                )
            )
            assertFalse(TwitchAuth.isModerator(event))
            assertFalse(TwitchAuth.isAuthorized(Permission.VIP, event))
            assertFalse(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
        }

        @Test
        @DisplayName("should handle user with multiple roles correctly")
        fun multipleRoles() {
            // A moderator who is also a subscriber
            val event = createMockEvent(
                tags = mapOf(
                    "mod" to "1",
                    "subscriber" to "1",
                    "badges" to "moderator/1,subscriber/24"
                )
            )
            assertTrue(TwitchAuth.isAuthorized(Permission.MODERATOR, event))
            assertTrue(TwitchAuth.isAuthorized(Permission.SUBSCRIBER, event))
        }

        @Test
        @DisplayName("should handle broadcaster edge case with mock failure")
        fun broadcasterEdgeCase() {
            val event = mockk<ChannelMessageEvent>(relaxed = true)
            val user = mockk<EventUser>(relaxed = true)
            val channel = mockk<EventChannel>(relaxed = true)

            every { event.user } returns user
            every { event.channel } returns channel
            every { user.id } throws Exception("User ID unavailable")
            every { channel.id } returns "channel123"

            // Should safely return false when exception occurs
            assertFalse(TwitchAuth.isBroadcaster(event))
        }
    }
}
