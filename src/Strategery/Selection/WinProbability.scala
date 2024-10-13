package Strategery.Selection

import Lifecycle.With
import Mathematics.Maff
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
    val explorationGames = strategyBranch.explorationGames
    val meanWinProbability =
      Maff.geometricMean(strategyBranch.strategies.map(_.evaluation).map(eval =>
        Math.max(
          Maff.nanToZero(eval.gamesWeightedWon / eval.gamesWeighted),
          (eval.gamesWeightedWon + explorationGames * With.configuration.targetWinrate)
        / (eval.gamesWeighted    + explorationGames))))
    noise(meanWinProbability)
  }

  private def noise(input: Double): Double = {
    val noiseWeight = 1e-6
    Random.nextDouble() * noiseWeight + input * (1.0 - noiseWeight)
  }
}
