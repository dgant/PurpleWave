package Strategery.Strategies.Options.Protoss.VsTerran.PvTMacro

import Strategery.Strategies.{Strategy, StrategyChoice}

object PvTMacroLate extends StrategyChoice {
  
  override val options: Iterable[Strategy] = Vector(
    LateArbiters,
    LateCarriers,
    LateMassGateway)
  
}