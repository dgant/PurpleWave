package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object GooseChase extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    false
    && unit.canMove
    && unit.matchups.threats.nonEmpty
    && unit.matchups.threats.forall(unit.topSpeed > _.topSpeed * 1.5)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val paths = With.geography.zones.flatMap(With.paths.zonePath(unit.zone, _)).filter(_.steps.size > 1)
    if (paths.nonEmpty) {
      val gooseChasePath = paths.maxBy(path =>
        (if (path.to.edges.length > 1) 2.0 else 1.0)
        * (
          path.lengthPixels +
          With.geography.home.pixelCenter.groundPixels(path.steps.last.to.centroid)))
      
      unit.agent.toReturn = gooseChasePath.steps.lastOption.map(_.to.centroid.pixelCenter)
    }
  }
}
