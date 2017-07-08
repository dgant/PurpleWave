package Strategery.Strategies.Options.Protoss.PvP

import Strategery.Strategies.Strategy
import bwapi.Race

object PvPEarly1GateZZCore extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvPMidgame4GateGoons,
      PvPMidgameCarriers,
      PvPMidgameDarkTemplar,
      PvPMidgameObserverReaver,
      PvPMidgameReaver))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Protoss)
}
