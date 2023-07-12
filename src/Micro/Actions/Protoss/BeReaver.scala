package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeReaver extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Reaver(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.ride.isEmpty) return
    if (unit.transport.isDefined) return
    lazy val canShoot             = unit.scarabs > 0 && unit.matchups.targetsInRange.nonEmpty
    lazy val inRangeOfPlebian     = unit.matchups.threatsInRange.exists(t => ! t.flying && t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t))
    lazy val needRefresh          = unit.cooldownLeft > 30 + 2 * With.latency.latencyFrames // Reaver cooldown resets to 30 when dropped: https://github.com/OpenBW/openbw/blob/master/bwgame.h#L3165
    lazy val needlesslyEndangered = needRefresh && (inRangeOfPlebian || ! canShoot)
    lazy val needlesslyDoomed     = unit.cooldownLeft >= unit.doomedInFrames
    if ( ! unit.agent.commit && (needlesslyEndangered || needlesslyDoomed || unit.matchups.targetedByScarab)) {
      unit.agent.act("Hop")
      demandPickup(unit)
      Retreat(unit)
    } else if (needRefresh) {
      unit.agent.act("Refresh")
      demandPickup(unit)
    }
  }

  def demandPickup(unit: FriendlyUnitInfo): Boolean = {
    val ride = unit.agent.ride.filter(_.agent.passengersPrioritized.find( ! _.loaded).contains(unit))
    ride.foreach(Commander.rightClick(unit, _))
    ride.isDefined
  }
}
