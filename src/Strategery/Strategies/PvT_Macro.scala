package Strategery.Strategies

import Strategery.Strategies.Options.PvT_Macro.{PvT_MacroEarly, PvT_MacroLate}
import Strategery.Strategies.Options.StrategyFeature
import bwapi.Race

object PvT_Macro extends Strategy {
  
  override def features: Iterable[StrategyFeature] = Vector(PvT_MacroEarly, PvT_MacroLate)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}
