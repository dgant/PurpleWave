package Strategery.Strategies.Options.Zerg.Zerg

import Strategery.Strategies.Strategy
import bwapi.Race

object PvZOvergas1HatchMuta extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
}
