package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object GooseChase extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (unit.isAny(Terran.Vulture, Zerg.Zergling) || unit.agent.canScout)
    && With.strategy.map.forall(_.trustGroundDistance)
    && unit.canMove
    && unit.matchups.threats.nonEmpty
    && unit.matchups.threats.forall(unit.topSpeed > _.topSpeed * 1.09) // Lets a Zergling just barely goose chase a Dragoon
    && ByOption.min(With.geography.ourBases   .map(base => unit.tileIncludingCenter.groundPixels(base.heart))).getOrElse(0.0) >
       ByOption.min(With.geography.enemyBases .map(base => unit.tileIncludingCenter.groundPixels(base.heart))).getOrElse(32.0 * 256.0)
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
