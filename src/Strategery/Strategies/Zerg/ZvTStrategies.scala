package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvT.{ZvT13PoolMuta, ZvT1HatchHydra, ZvT1HatchLurker, ZvT2HatchLingBustMuta, ZvT2HatchLurker, ZvT3HatchLing}
import Strategery.Strategies.Strategy
import Strategery.{Heartbreak, StarCraftMap}
import bwapi.Race

abstract class ZvTStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Vector(Race.Zerg)
  override def enemyRaces: Seq[Race] = Vector(Race.Terran)
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
object ZvT3HatchLing extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT3HatchLing)
  override def mapsBlacklisted: Seq[StarCraftMap] = Seq(Heartbreak)
}
object ZvT2HatchLurker extends ZvTStrategy {
  override def gameplan: Option[Plan] = Some(new ZvT2HatchLurker)
}

abstract class ZvTOpening extends ZvTStrategy

object ZvT7Pool extends ZvTOpening
object ZvT12Hatch13Pool extends ZvTOpening
object ZvT12Hatch11Pool extends ZvTOpening
object ZvT9Pool extends ZvTOpening

