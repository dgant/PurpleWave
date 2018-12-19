package Strategery.Selection

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.Strategies.Strategy

object StrategySelectionDynamic extends StrategySelectionBasic {

  override protected def chooseBasedOnInterest: Iterable[Strategy] = {
    val strategies: Seq[(Iterable[Strategy], Double)] = With.strategy.interest.toVector
    PurpleMath
      .softmaxSample[(Iterable[Strategy], Double)](
        strategies,
        v => With.configuration.dynamicStickiness * v._2)
      .map(_._1)
      .getOrElse(Iterable.empty)
  }
}