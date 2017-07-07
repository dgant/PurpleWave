package Strategery.Strategies.Options.PvTMacro

import Strategery.Strategies.Options.StrategyFeature
import Strategery.Strategies.Strategy

object PvTMacroLate extends StrategyFeature {
  
  override val options: Iterable[Strategy] = Vector(
    LateArbiters,
    LateCarriers,
    LateMassGateway)
  
}