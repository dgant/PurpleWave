package Strategery.Strategies.Options.PvTMacro

import Strategery.Strategies.Options.StrategyFeature
import Strategery.Strategies.Strategy

object PvTMacroEarly extends StrategyFeature {
  
  override val options: Iterable[Strategy] = Vector(
    Early14Nexus,
    EarlyDarkTemplar,
    Early1GateRange,
    Early1015GateGoon)
  
}
