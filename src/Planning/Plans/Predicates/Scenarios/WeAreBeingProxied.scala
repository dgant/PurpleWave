package Planning.Plans.Predicates.Scenarios

import Lifecycle.With
import Planning.Predicate
import Utilities.ByOption

class WeAreBeingProxied extends Predicate {
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit => {
      lazy val isBuilding     = unit.unitClass.isBuilding
      lazy val tileBuilding   = unit.tileIncludingCenter
      lazy val tileComparison = With.geography.enemyBases.headOption
        .orElse(ByOption.maxBy(With.geography.startBases.filterNot(_.owner.isUs))(_.heart.tileDistanceFast(With.geography.home)))
        .map(_.heart)
      lazy val distanceUs     = With.geography.home.tileDistanceSquared(tileBuilding)
      lazy val distanceEnemy  = tileComparison.map(_.tileDistanceSquared(tileBuilding)).getOrElse(0)
      lazy val closerToUs     = distanceUs < distanceEnemy
      
      isBuilding && closerToUs
    })
  }
}
