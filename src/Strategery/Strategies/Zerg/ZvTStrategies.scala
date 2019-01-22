package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.{OneHatchLurker, ProxyHatch}
import Planning.Plans.GamePlans.Zerg.ZvT.ZvT13PoolMuta
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvTStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}

object ZvT13PoolMuta extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT13PoolMuta)
}
object ZvT1HatchLurker extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new OneHatchLurker)
}
object ZvTProxyHatchSunkens extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
}
object ZvTProxyHatchHydras extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
}
object ZvTProxyHatchZerglings extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
}