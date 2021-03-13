package Micro.Actions.Combat.Targeting.Filters
import Lifecycle.With
import Planning.UnitMatchers.MatchSiegeTank
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object TargetFilterVsTank extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = ! actor.flying
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! target.player.isTerran) return true
    if (target.unitClass.suicides) return true
    if (With.reaction.sluggishness > 0) return true
    lazy val firingPixel = actor.pixelToFireAt(target)
    (
      target.isAny(MatchSiegeTank, Terran.Battlecruiser)
      || target.order == Orders.Repair && target.orderTarget.exists(_.isAny(MatchSiegeTank, Terran.Battlecruiser))
      || ! actor.matchups.threats.exists(t => Terran.SiegeTankSieged(t) && t.inRangeToAttack(actor, firingPixel)))
  }
}
