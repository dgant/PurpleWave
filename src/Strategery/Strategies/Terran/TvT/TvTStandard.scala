package Strategery.Strategies.Terran.TvT

import Strategery.Strategies.Strategy
import bwapi.Race

object TvTStandard extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}
