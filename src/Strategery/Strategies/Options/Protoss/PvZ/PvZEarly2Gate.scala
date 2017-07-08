package Strategery.Strategies.Options.Protoss.PvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object PvZEarly2Gate extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvZMidgame5GateDragoons,
      PvZMidgameCorsairReaver,
      PvZMidgameCorsairSpeedlot))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Zerg)
}
