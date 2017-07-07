package Strategery.Strategies

import Strategery.Strategies.Options.PvTMacro.{PvTMacroEarly, PvTMacroLate}
import Strategery.Strategies.Options.StrategyFeature
import bwapi.Race

object PvT_Macro extends Strategy {
  
  override def features: Iterable[StrategyFeature] = Vector(PvTMacroEarly, PvTMacroLate)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}
