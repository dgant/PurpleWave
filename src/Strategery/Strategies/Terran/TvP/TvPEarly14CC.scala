package Strategery.Strategies.Terran.TvP

import Strategery.Strategies.Strategy
import bwapi.Race

object TvPEarly14CC extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvPMidgameBioTank))
}
