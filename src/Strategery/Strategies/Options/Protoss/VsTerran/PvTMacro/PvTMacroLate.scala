package Strategery.Strategies.Options.Protoss.VsTerran.PvTMacro

import Strategery.Strategies.Options.Protoss.VsTerran.PvTMacro.Late.{LateArbiters, LateCarriers, LateMassGateway}
import Strategery.Strategies.{Strategy, StrategyChoice}

object PvTMacroLate extends StrategyChoice {
  
  override val options: Iterable[Strategy] = Vector(
    LateArbiters,
    LateCarriers,
    LateMassGateway)
  
}