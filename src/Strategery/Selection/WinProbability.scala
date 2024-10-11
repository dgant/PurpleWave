package Strategery.Selection

import Lifecycle.With
import Mathematics.Maff
import Strategery.History.HistoricalGame
import Strategery.Strategies.StrategyBranch

import scala.util.Random

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

  def apply(strategyBranch: StrategyBranch): Double = {
    noise(ucb(strategyBranch))
  }

  private def noise(input: Double): Double = {
    val noiseWeight = 1e-6
    Random.nextDouble() * noiseWeight + input * (1.0 - noiseWeight)
  }

  // The approach we used before UCB
  private def geometricMean(strategyBranch: StrategyBranch): Double = {
    Maff.geometricMean(strategyBranch.strategies.map(_.evaluation).map(e =>
        (e.gamesWeightedWon + 1.5 * With.configuration.targetWinrate)
      / (e.gamesWeighted    + 1.5)))
  }

  private def ucb(strategyBranch: StrategyBranch): Double = {
    val strategies = strategyBranch.strategies
    class WeightedGame(val game: HistoricalGame) {
      val similarity  : Double = strategies.count(game.weEmployed).toDouble / strategies.size
      val finalWeight : Double = similarity * game.weight
    }

    val expectedWins = With.configuration.targetWinrate

    val games = With.strategy.gamesVsOpponent
      .filter(game => strategies.exists(game.weEmployed))
      .map(new WeightedGame(_))
      .toVector

    if (games.isEmpty) return expectedWins

    // UCB Confidence:
    // Lower values (like 0.5 - 1) -> More exploitation
    // Higher values (like 1.5 - 2) -> More exploration
    val totalBranches = With.strategy.strategyBranchesLegal.length
    val totalGames    = 200 // TODO: Configure per event
    val ucbConfidence = 1.0
    //explorationConstant * Math.sqrt(2 * Math.log(totalGames) * totalBranches / totalGames)

    // Calculate UCB
    val weightTotal     = games.map(_.finalWeight).sum
    val weightWins      = games.filter(_.game.won).map(_.finalWeight).sum
    val winRate         = Maff.nanToN(weightWins / weightTotal, expectedWins)
    val weightedSamples = weightTotal / games.map(_.finalWeight).max
    val ucb             = winRate + ucbConfidence * Math.sqrt(2 * Math.log(games.length) / weightedSamples)

    Maff.clamp01(ucb)
  }

}
