package Micro.Actions.Combat.Decisionmaking

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Follow extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.unitClass.followingAllowed
    && unit.canMove
    && ! unit.matchups.groupVs.splashesAir
    && unit.agent.leader().exists(unit !=)
    && unit.matchups.threatsInRange.forall(_.inRangeToAttack(unit.agent.leader().get)))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val maybeLeader = unit.agent.leader()
    unit.agent.toTravel = maybeLeader.map(_.pixel).orElse(unit.agent.toTravel)
    maybeLeader
      .filter(leader =>
        unit.pixelDistanceCenter(leader) < Seq(256, Maff.min(unit.matchups.threats.view.map(_.pixelsToGetInRange(unit).toInt)).getOrElse(0)).max
        && ( ! unit.is(Protoss.Carrier) || leader.matchups.threatsInRange.forall(unit.matchups.threatsInRange.contains))
      )
      .foreach(leader => {
        if (unit.matchups.targetsInRange.isEmpty
          && unit.matchups.framesOfSafety > 24
          && unit.pixelDistanceCenter(leader) > 64) {
          Commander.move(unit)
        }
        leader.agent.leadFollower(unit)
      })
  }
}
