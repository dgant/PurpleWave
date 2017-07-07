package Strategery.Strategies.Options.Protoss.PvTMacro

import Strategery.Strategies.{Strategy, StrategyChoice}

object PvTMacroEarly extends StrategyChoice {
  
  override val options: Iterable[Strategy] = Vector(
    Early14Nexus,
    EarlyDTExpand,
    Early1GateRange,
    Early1015GateGoon)
  
}
