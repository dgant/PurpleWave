package Strategery.Strategies.Protoss.PvP

import Strategery.Strategies.Strategy
import bwapi.Race

object PvPEarly2Gate1012 extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvPMidgame4GateGoon,
      PvPMidgameDarkTemplar,
      PvPMidgameFE,
      PvPMidgameObserverReaver,
      PvPMidgameReaver))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Protoss)
}
