package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object BeReaver extends Action {

  val cooldownOnLanding = 30

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Reaver(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.ride.forall(_.pixelDistanceEdge(unit) > ?(unit.agent.isPrimaryPassenger, 64, 32))) return
    if (unit.transport.isDefined) return
    lazy val canShoot             = unit.scarabs > 0 && unit.matchups.targetsInRange.nonEmpty
    lazy val inRangeOfPlebian     = unit.matchups.threatsInRange.exists(t => ! t.flying && t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t))
    lazy val needRefresh          = unit.cooldownLeft > cooldownOnLanding + 2 * With.latency.latencyFrames // Reaver cooldown resets to 30 when dropped: https://github.com/OpenBW/openbw/blob/master/bwgame.h#L3165
    lazy val needlesslyEndangered = needRefresh && (inRangeOfPlebian || ! canShoot)
    lazy val needlesslyDoomed     = unit.cooldownLeft >= unit.doomedInFrames
    if ( ! unit.agent.commit && (needlesslyEndangered || needlesslyDoomed || unit.matchups.targetedByScarab)) {
      unit.agent.act("Hop")
      clickShuttle(unit)
    } else if (needRefresh) {
      unit.agent.act("Refresh")
      clickShuttle(unit)
    }
  }

  def clickShuttle(unit: FriendlyUnitInfo): Unit = {
    Commander.rightClick(unit, unit.agent.ride.get)
  }
}
