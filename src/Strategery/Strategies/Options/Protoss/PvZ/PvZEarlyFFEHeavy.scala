package Strategery.Strategies.Options.Protoss.PvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object PvZEarlyFFEHeavy extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvZMidgameCorsairCarrier,
      PvZMidgameCorsairDarkTemplar,
      PvZMidgameCorsairReaver,
      PvZMidgameCorsairSpeedlot))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
