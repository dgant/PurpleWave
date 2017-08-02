package Strategery.Strategies.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.Zerg2HatchMuta
import Strategery.Strategies.Strategy
import bwapi.Race

object Zerg2HatchMutaAllIn extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def buildGameplan(): Option[Plan] = Some(new Zerg2HatchMuta)
}