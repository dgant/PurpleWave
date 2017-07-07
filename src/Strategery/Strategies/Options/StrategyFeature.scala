package Strategery.Strategies.Options

import Strategery.Strategies.Strategy

abstract class StrategyFeature {
  
  val options: Iterable[Strategy]
}
