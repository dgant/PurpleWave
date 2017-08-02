package Strategery.Strategies.Protoss.PvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object PvZEarlyFFEConservative extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossVsZergChoices.midgames
  )
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
