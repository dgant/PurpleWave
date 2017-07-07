package Strategery.Strategies.Options.PvT_Macro

import Strategery.Strategies.Options.StrategyFeature
import Strategery.Strategies.Strategy

object PvT_MacroLate extends StrategyFeature {
  
  override val options: Iterable[Strategy] = Vector(
    LateArbiters,
    LateCarriers,
    LateMassGateway)
  
}