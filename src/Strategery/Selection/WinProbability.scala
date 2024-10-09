package Strategery.Selection

import Lifecycle.With

import scala.util.Random
import Mathematics.Maff
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

object WinProbability {

  // Naive Bayes probability of winning with multiple strategies
  // Explanation at http://cs.wellesley.edu/~anderson/writing/naive-bayes.pdf
  // Bayes: P(A|B) = P(B|A) * P(A) / P(B)
  // Bayes: P(Win|Build) = P(Build|Win) * P(Win) / P(Build)
  // P(Win|A & B) = P(A & B|Win) * P(Win) / P(A & B)
  // P(Win|A & B & C) = P(A & B & C|Win) * P(Win) / P(A & B & C)
  // We can try introducing priors for P(A & B & C)
  // Conditional independence assumption used in Naive Bayes: P(A & B|Win) = P(A|win)P(B|win)
  // Naive Bayes: P(Win|A & B) = P(win)P(A|win)P(B|win) / ( P(win)P(A|win)P(B|win) + P(loss)P(A|loss)P(B|loss) )
  // Naive Bayes assumes independence of A/B in each class, which for us is not very true but makes better use of limited game information
  // When we have an untested strategy, we can be optimistic in the face of uncertainty: P(A|win) == goal_wr and P(B|win) == 1 - goal_wr
  //
  // Apply a small random factor to shuffle strategies with nearly-equal values

  def apply(strategies: Iterable[Strategy]): Double = {
    noise(ucb(strategies))
  }

  val noiseWeight = 1e-6
  private def noise(input: Double): Double = {
    Random.nextDouble() * noiseWeight + input * (1.0 - noiseWeight)
  }

  // The approach we used before UCB
  private def geometricMean(strategies: Iterable[Strategy]): Double = {
    Maff.geometricMean(strategies.map(_.evaluation.probabilityWin))
  }

  private def ucb(strategies: Iterable[Strategy]): Double = {
    class WeightedGame(val game: HistoricalGame, val age: Int) {
      val relevanceSimilarity : Double = strategies.count(game.weEmployed).toDouble / strategies.size
      val relevanceTimeDecay  : Double = Math.pow(0.98, age)
      val relevanceRecentLoss : Double = Maff.fromBoolean(age < With.configuration.recentFingerprints && !game.won)
      val relevanceAge        : Double = 0.25 + 0.5 * relevanceTimeDecay + 0.25 * relevanceRecentLoss
      val weight              : Double = relevanceSimilarity * relevanceAge
    }

    val expectedWins = 0.5 // TODO: Use baseline expected WR per opponent

    val games = With.strategy.gamesVsOpponent
      .filter(game => strategies.exists(game.weEmployed))
      .zipWithIndex
      .map { case (game, index) => new WeightedGame(game, index) }
      .toVector

    if (games.isEmpty) return expectedWins

    val weightTotal = games.map(_.weight).sum
    val weightWins  = games.filter(_.game.won).map(_.weight).sum

    val winRate = if (weightTotal > 0) weightWins / weightTotal else 0.5

    // Calculate adjusted sample size
    val denominator = weightTotal / games.map(_.weight).max

    // UCB Confidence:
    // Lower values (like 0.5 - 1) -> More exploitation
    // Higher values (like 1.5 - 2) -> More exploration
    val ucbConfidence = 1.0

    // Calculate UCB
    val ucb = winRate + ucbConfidence * Math.sqrt(2 * Math.log(games.length) / denominator)

    Maff.clamp01(ucb)
  }

}
