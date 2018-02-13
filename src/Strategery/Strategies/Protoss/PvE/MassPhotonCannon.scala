package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.MassPhotonCannon
import Strategery.Strategies.Strategy
import bwapi.Race

object MassPhotonCannon extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new MassPhotonCannon) }
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
}
