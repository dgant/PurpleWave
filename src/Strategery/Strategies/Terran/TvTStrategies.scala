package Strategery.Strategies.Terran

import Planning.Plan
import Planning.Plans.GamePlans.Terran.TvT.TvTProxy5Rax
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class TvTStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}

abstract class TvTOpening extends TvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    TvT5Fac, TvT2Base2Port, TvT2BaseBC
  ))
}

object TvTProxy5Rax extends TvTStrategy {
  override def gameplan: Option[Plan] = Some(new TvTProxy5Rax)
  override def mapsBlacklisted: Vector[StarCraftMap] = MapGroups.badForProxying
}


object TvT14CC extends TvTOpening
object TvT1RaxFE extends TvTOpening
object TvT1FacFE extends TvTOpening
object TvT1FacPort extends TvTOpening
object TvT2FacTanks extends TvTOpening
object TvT2Port extends TvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    TvT2Base2Port, TvT2BaseBC
  ))
}
object TvT2Base2Port extends TvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    TvT2BaseBC
  ))
}
object TvT5Fac extends TvTStrategy
object TvT2BaseBC extends TvTStrategy