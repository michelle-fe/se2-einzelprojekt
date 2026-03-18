package at.aau.serg.services

import at.aau.serg.models.GameResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameResultServiceTests {

    private lateinit var service: GameResultService

    @BeforeEach
    fun setup() {
        service = GameResultService()
    }

    @Test
    fun test_getGameResults_emptyList() {
        val result = service.getGameResults()

        assertEquals(emptyList<GameResult>(), result)
    }

    @Test
    fun test_addGameResult_getGameResults_containsSingleElement() {
        val gameResult = GameResult(1, "player1", 17, 15.3)

        service.addGameResult(gameResult)
        val res = service.getGameResults()

        assertEquals(1, res.size)
        assertEquals(gameResult, res[0])
    }

    @Test
    fun test_getGameResultById_existingId_returnsObject() {
        val gameResult = GameResult(1, "player1", 17, 15.3)
        service.addGameResult(gameResult)

        val res = service.getGameResult(1)

        assertEquals(gameResult, res)
    }

    @Test
    fun test_getGameResultById_nonexistentId_returnsNull() {
        val gameResult = GameResult(1, "player1", 17, 15.3)
        service.addGameResult(gameResult)

        val res = service.getGameResult(22)

        assertNull(res)
    }

    @Test
    fun test_addGameResult_multipleEntries_correctId() {
        val gameResult1 = GameResult(0, "player1", 17, 15.3)
        val gameResult2 = GameResult(0, "player2", 25, 16.0)

        service.addGameResult(gameResult1)
        service.addGameResult(gameResult2)

        val res = service.getGameResults()

        assertEquals(2, res.size)

        assertEquals(gameResult1, res[0])
        assertEquals(1, res[0].id)

        assertEquals(gameResult2, res[1])
        assertEquals(2, res[1].id)
    }

    @Test
    fun test_getLeaderboard_sortsByScoreDescending() {
        // Prüft, ob Ergebnisse primär nach Score absteigend sortiert werden
        val low = GameResult(0, "low", 10, 15.0)
        val high = GameResult(0, "high", 30, 15.0)
        val mid = GameResult(0, "mid", 20, 15.0)

        service.addGameResult(low)
        service.addGameResult(high)
        service.addGameResult(mid)

        val leaderboard = service.getLeaderboard()

        assertEquals(3, leaderboard.size)
        assertEquals("high", leaderboard[0].playerName)
        assertEquals("mid", leaderboard[1].playerName)
        assertEquals("low", leaderboard[2].playerName)
    }

    @Test
    fun test_getLeaderboard_sameScore_sortsByTimeAscending() {
        // Prüft, ob bei gleichem Score sekundär nach Zeit aufsteigend sortiert wird
        val slow = GameResult(0, "slow", 20, 30.0)
        val fast = GameResult(0, "fast", 20, 10.0)
        val medium = GameResult(0, "medium", 20, 20.0)

        service.addGameResult(slow)
        service.addGameResult(fast)
        service.addGameResult(medium)

        val leaderboard = service.getLeaderboard()

        assertEquals(3, leaderboard.size)
        assertEquals("fast", leaderboard[0].playerName)
        assertEquals("medium", leaderboard[1].playerName)
        assertEquals("slow", leaderboard[2].playerName)
    }

    @Test
    fun test_getLeaderboard_complexSorting() {
        // Kombinierter Test für Score-Abstieg und Zeit-Aufstieg
        val r1 = GameResult(0, "r1", 100, 50.0)
        val r2 = GameResult(0, "r2", 100, 30.0)
        val r3 = GameResult(0, "r3", 50, 10.0)
        val r4 = GameResult(0, "r4", 50, 20.0)

        service.addGameResult(r1)
        service.addGameResult(r2)
        service.addGameResult(r3)
        service.addGameResult(r4)

        val leaderboard = service.getLeaderboard()

        assertEquals("r2", leaderboard[0].playerName)
        assertEquals("r1", leaderboard[1].playerName)
        assertEquals("r3", leaderboard[2].playerName)
        assertEquals("r4", leaderboard[3].playerName)
    }

    @Test
    fun test_getLeaderboardRange_validRank_returnsCorrectSlice() {
        // Erstellt 10 Ergebnisse
        for (i in 1..10) {
            service.addGameResult(GameResult(0, "p$i", 100 - i, 10.0))
        }

        // Leaderboard: p1 (1), p2 (2), p3 (3), p4 (4), p5 (5), p6 (6), p7 (7), p8 (8), p9 (9), p10 (10)
        // Rang 5: Index 4. Erwartet: 5 +/- 3 -> Rang 2 bis 8 (Indizes 1 bis 7)
        val range = service.getLeaderboardRange(5)

        assertEquals(7, range.size)
        assertEquals("p2", range[0].playerName) // Rang 2
        assertEquals("p5", range[3].playerName) // Rang 5
        assertEquals("p8", range[6].playerName) // Rang 8
    }

    @Test
    fun test_getLeaderboardRange_rankAtStart_clamped() {
        for (i in 1..10) {
            service.addGameResult(GameResult(0, "p$i", 100 - i, 10.0))
        }

        // Rang 1: Erwartet Rang 1 bis 4 (Index 0 bis 3)
        val range = service.getLeaderboardRange(1)
        assertEquals(4, range.size)
        assertEquals("p1", range[0].playerName)
        assertEquals("p4", range[3].playerName)
    }

    @Test
    fun test_getLeaderboardRange_rankAtEnd_clamped() {
        for (i in 1..10) {
            service.addGameResult(GameResult(0, "p$i", 100 - i, 10.0))
        }

        // Rang 10: Erwartet Rang 7 bis 10 (Index 6 bis 9)
        val range = service.getLeaderboardRange(10)
        assertEquals(4, range.size)
        assertEquals("p7", range[0].playerName)
        assertEquals("p10", range[3].playerName)
    }

    @Test
    fun test_getLeaderboardRange_invalidRank_throwsException() {
        service.addGameResult(GameResult(0, "p1", 100, 10.0))

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            service.getLeaderboardRange(0)
        }
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            service.getLeaderboardRange(2)
        }
    }

    @Test
    fun test_getLeaderboardRange_smallList_returnsEntireList() {
        service.addGameResult(GameResult(0, "p1", 100, 10.0))
        service.addGameResult(GameResult(0, "p2", 90, 10.0))

        val range = service.getLeaderboardRange(1)
        assertEquals(2, range.size)
    }

    @Test
    fun test_getLeaderboardRange_rankTooHigh_throwsException() {
        service.addGameResult(GameResult(0, "p1", 100, 10.0))
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            service.getLeaderboardRange(2)
        }
    }

    @Test
    fun test_deleteGameResult_removesCorrectEntry() {
        val gameResult1 = GameResult(0, "p1", 100, 10.0)
        val gameResult2 = GameResult(0, "p2", 200, 20.0)
        service.addGameResult(gameResult1)
        service.addGameResult(gameResult2)

        val deleted = service.deleteGameResult(1)
        val remaining = service.getGameResults()

        assertEquals(true, deleted)
        assertEquals(1, remaining.size)
        assertEquals(2, remaining[0].id)
    }

}