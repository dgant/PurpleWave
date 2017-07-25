package Strategery.Strategies.Options.Zerg.Global

import Strategery.Strategies.Strategy
import bwapi.Race

object PvZ4PoolAllIn extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
}
