package Strategery.Selection

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.Strategies.Strategy

object StrategySelectionRandom extends StrategySelectionBasic {

  override protected def chooseBasedOnInterest: Iterable[Strategy] = {
    PurpleMath.sample(With.strategy.interest.toVector.map(_._1))
  }
}