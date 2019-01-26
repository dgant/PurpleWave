package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.{ZvP2HatchMuta, ZvP3Hatch, ZvP6Hatch}
import Strategery.{Destination, Heartbreak, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvPStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
object ZvP3Hatch extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP3Hatch) }
}
object ZvP6Hatch extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP6Hatch) }
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Destination, Heartbreak)
}
object ZvP2HatchMuta extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP2HatchMuta) }
}