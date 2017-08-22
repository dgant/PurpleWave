package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Detect extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.unitClass.isDetector
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    var spookies = unit.squadmates.flatMap(_.matchups.enemies).toSet.filter(_.effectivelyCloaked).toSeq
    if (spookies.isEmpty) {
      spookies = unit.matchups.enemies.filter(_.effectivelyCloaked)
    }
    if (spookies.nonEmpty) {
      val spookiest = spookies.minBy(x => ( ! x.canAttack, x.pixelDistanceFast(unit)))
      val vantage   = spookiest.pixelCenter
      unit.agent.toTravel = Some(vantage)
      Move.delegate(unit)
    }
  }
}
