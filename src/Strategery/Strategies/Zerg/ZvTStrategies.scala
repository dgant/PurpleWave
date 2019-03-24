package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvT.{ZvT13PoolMuta, ZvT1HatchHydra, ZvT1HatchLurker, ZvT2HatchLingBustMuta, ZvT2HatchLurker, ZvT3HatchLing, ZvT7Pool, ZvTProxyHatch}
import Strategery.{Heartbreak, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvTStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}

object ZvT13PoolMuta extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT13PoolMuta)
}
object ZvT1HatchHydra extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT1HatchHydra)
  override def allowedVsHuman: Boolean = false
}
object ZvT1HatchLurker extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT1HatchLurker)
}
object ZvT2HatchLingBustMuta extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT2HatchLingBustMuta)
}
object ZvT7Pool extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT7Pool)
}
object ZvT3HatchLing extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT3HatchLing)
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Heartbreak)
}
object ZvT2HatchLurker extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT2HatchLurker)
}
object ZvTProxyHatchSunkens extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvTProxyHatch)
  override def startLocationsMax: Int = 3
}
object ZvTProxyHatchHydras extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvTProxyHatch)
  override def startLocationsMax: Int = 3
}
object ZvTProxyHatchZerglings extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvTProxyHatch)
  override def startLocationsMax: Int = 2
}