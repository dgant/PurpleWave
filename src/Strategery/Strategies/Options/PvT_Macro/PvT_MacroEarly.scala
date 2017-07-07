package Strategery.Strategies.Options.PvT_Macro

import Strategery.Strategies.Options.StrategyFeature
import Strategery.Strategies.Strategy

object PvT_MacroEarly extends StrategyFeature {
  
  override val options: Iterable[Strategy] = Vector(
    Early14Nexus,
    EarlyDarkTemplar,
    Early1GateRange,
    Early1015GateGoon)
  
}
