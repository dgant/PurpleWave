package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.ProxyDarkTemplarRush
import Strategery.Strategies.Protoss.{PvPGateDTExpand, PvT2BaseArbiter}
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyDarkTemplar extends Strategy {

  override def gameplan: Option[Plan] = { Some(new ProxyDarkTemplarRush) }

  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvT2BaseArbiter, PvPGateDTExpand))
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
  
  override def mapsBlacklisted: Vector[StarCraftMap] = MapGroups.badForProxying
}
