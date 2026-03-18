package at.aau.serg.services

import at.aau.serg.models.GameResult
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class GameResultService {

    private val gameResults = mutableListOf<GameResult>()
    private var nextId = AtomicLong(1)

    fun addGameResult(gameResult: GameResult) {
        gameResult.id = nextId.getAndIncrement()
        gameResults.add(gameResult)
    }

    fun getGameResult(id: Long): GameResult? = gameResults.find { it.id == id } // ? allows null

    fun getGameResults(): List<GameResult> = gameResults.toList() // returns immutable list copy

    /**
     * Gibt das Leaderboard zurück.
     * Die Sortierung erfolgt primär absteigend nach dem Score (höher ist besser).
     * Bei gleichem Score erfolgt die Sortierung sekundär aufsteigend nach der Zeit (schneller ist besser).
     */
    fun getLeaderboard(): List<GameResult> =
        gameResults.sortedWith(compareBy({ -it.score }, { it.timeInSeconds }))

    /**
     * Gibt einen Ausschnitt des Leaderboards zurück.
     * @param rank Der 1-basierte Rang des Spielers.
     * @return Eine Liste mit dem Spieler auf dem Rang sowie bis zu 3 Spielern davor und danach.
     */
    fun getLeaderboardRange(rank: Int): List<GameResult> {
        val leaderboard = getLeaderboard()
        if (rank <= 0 || rank > leaderboard.size) {
            throw IllegalArgumentException("Ungültiger Rang: $rank")
        }

        val startIndex = (rank - 4).coerceAtLeast(0)
        val endIndex = (rank + 3).coerceAtMost(leaderboard.size)

        return leaderboard.subList(startIndex, endIndex)
    }

    /**
     * Kotlin-idiomatic for:
     * fun deleteGameResult(gameResultId: Long) {
     *     gameResults.removeIf({ gameResult -> gameResult.id == gameResultId })
     * }
     */
    fun deleteGameResult(id: Long) = gameResults.removeIf { it.id == id }

}