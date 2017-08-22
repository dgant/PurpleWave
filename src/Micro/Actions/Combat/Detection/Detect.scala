package Micro.Actions.Combat.Detection

import Micro.Actions.Action
import Micro.Actions.Commands.Travel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Detect extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.unitClass.isDetector
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    var spookies = unit.squad.flatMap(_.enemies).filter(_.effectivelyCloaked)
    if (spookies.isEmpty) {
      spookies = unit.matchups.enemies.filter(_.effectivelyCloaked)
    }
    if (spookies.nonEmpty) {
      val spookiest = spookies.min(Ordering.by(x => ( ! x.canAttack, x.pixelDistanceFast(unit))))
      val vantage   = spookiest.pixelCenter
      unit.agent.toTravel = Some(vantage)
      Travel.delegate(unit)
    }
  }
}
