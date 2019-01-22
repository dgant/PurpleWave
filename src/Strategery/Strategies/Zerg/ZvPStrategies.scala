package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.{ZvPOverpool, ZvP3HatchAggro, ZvP2HatchMuta}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvPStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
object ZvP3HatchAggro extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP3HatchAggro) }
}
object ZvP2HatchMuta extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP2HatchMuta) }
}
object ZvPOverpool extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvPOverpool) }
}