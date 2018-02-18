package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Disengage
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreserveScout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (unit.matchups.framesOfSafetyDiffused <= 18
      && unit.matchups.threats.exists( ! _.unitClass.isWorker))
      || unit.totalHealth < 10
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val paths = With.geography.zones.flatMap(With.paths.zonePath(unit.zone, _)).filter(_.steps.size > 1)
    if (paths.nonEmpty) {
      val gooseChasePath = paths.maxBy(path =>
        path.lengthPixels +
          With.geography.home.pixelCenter.groundPixels(path.steps.last.to.centroid))
      unit.agent.toReturn = gooseChasePath.steps.lastOption.map(_.to.centroid.pixelCenter)
    }
    Disengage.delegate(unit)
  }
}
