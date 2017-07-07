package Strategery.Strategies.Options.Protoss.Choices

import Strategery.Strategies.Options.Protoss.PvTMacro.{PvTMacroEarly, PvTMacroLate}
import Strategery.Strategies.{Strategy, StrategyChoice}
import bwapi.Race

object PvT_Macro extends Strategy {
  
  override def features: Iterable[StrategyChoice] = Vector(PvTMacroEarly, PvTMacroLate)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}
