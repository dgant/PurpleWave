package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeAReaver extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Reaver) && unit.agent.ride.exists(_.canTransport(unit))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val inRangeNeedlessly = unit.matchups.threatsInRange.exists(t => ! t.flying && t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t))
    lazy val attackingSoon = unit.matchups.targetsInRange.nonEmpty && unit.cooldownLeft < Math.min(unit.cooldownMaxGround / 4, unit.matchups.framesToLive)
    if (inRangeNeedlessly && ! attackingSoon) {
      unit.agent.ride.foreach(With.commander.rightClick(unit, _))
    }
  }
}
