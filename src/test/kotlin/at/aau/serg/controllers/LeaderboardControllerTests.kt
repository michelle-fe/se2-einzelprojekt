package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever
import kotlin.test.assertEquals
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ControllerTests {

    private lateinit var mockedService: GameResultService
    private lateinit var leaderboardController: LeaderboardController
    private lateinit var gameResultController: GameResultController

    @BeforeEach
    fun setup() {
        mockedService = mock(GameResultService::class.java)
        leaderboardController = LeaderboardController(mockedService)
        gameResultController = GameResultController(mockedService)
    }

    // --- Tests für LeaderboardController ---

    @Test
    fun test_getLeaderboard_callsService() {
        // Testet, ob der Controller den Aufruf ohne Rank korrekt an den GameResultService delegiert
        val results = listOf(GameResult(1, "first", 20, 20.0))
        whenever(mockedService.getLeaderboard()).thenReturn(results)

        val res = leaderboardController.getLeaderboard(null)

        verify(mockedService).getLeaderboard()
        assertEquals(results, res)
    }

    @Test
    fun test_getLeaderboard_withRank_callsServiceRange() {
        // Testet, ob der Controller mit Rank den Service-Bereich aufruft
        val results = listOf(GameResult(1, "target", 100, 10.0))
        whenever(mockedService.getLeaderboardRange(1)).thenReturn(results)

        val res = leaderboardController.getLeaderboard(1)

        verify(mockedService).getLeaderboardRange(1)
        assertEquals(results, res)
    }

    @Test
    fun test_getLeaderboard_invalidRank_throwsBadRequest() {
        // Testet, ob ein ungültiger Rang eine BAD_REQUEST-Exception auslöst
        whenever(mockedService.getLeaderboardRange(-1)).thenThrow(IllegalArgumentException("Ungültiger Rang"))

        val exception = org.junit.jupiter.api.assertThrows<ResponseStatusException> {
            leaderboardController.getLeaderboard(-1)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    // --- Tests für GameResultController (für 100% Coverage) ---

    @Test
    fun test_getGameResult_callsService() {
        val expected = GameResult(1, "p1", 100, 10.0)
        whenever(mockedService.getGameResult(1)).thenReturn(expected)

        val actual = gameResultController.getGameResult(1)

        verify(mockedService).getGameResult(1)
        assertEquals(expected, actual)
    }

    @Test
    fun test_getAllGameResults_callsService() {
        val expected = listOf(GameResult(1, "p1", 100, 10.0))
        whenever(mockedService.getGameResults()).thenReturn(expected)

        val actual = gameResultController.getAllGameResults()

        verify(mockedService).getGameResults()
        assertEquals(expected, actual)
    }

    @Test
    fun test_addGameResult_callsService() {
        val gameResult = GameResult(0, "p1", 100, 10.0)
        gameResultController.addGameResult(gameResult)
        verify(mockedService).addGameResult(gameResult)
    }

    @Test
    fun test_deleteGameResult_callsService() {
        gameResultController.deleteGameResult(1)
        verify(mockedService).deleteGameResult(1)
    }
}
