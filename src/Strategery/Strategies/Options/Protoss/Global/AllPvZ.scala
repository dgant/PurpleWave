package Strategery.Strategies.Options.Protoss.Global

import Strategery.Strategies.Options.Protoss.PvZ.{Early2Gate, EarlyFFEHeavy, EarlyFFELight, EarlyZealotAllIn}
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvZ extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      Early2Gate,
      EarlyFFELight,
      EarlyFFEHeavy,
      EarlyZealotAllIn))
    
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
