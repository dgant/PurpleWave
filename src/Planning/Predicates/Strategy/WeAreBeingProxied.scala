package Planning.Predicates.Strategy

import Lifecycle.With
import Mathematics.Maff
import Planning.Predicate

class WeAreBeingProxied extends Predicate {
  
  override def apply: Boolean = {
    With.units.enemy.exists(unit => {
      lazy val scaryBuilding  = unit.unitClass.isBuilding && ! unit.unitClass.isGas
      lazy val tileBuilding   = unit.tile
      lazy val tileComparison = With.geography.enemyBases.headOption
        .orElse(Maff.maxBy(With.geography.startBases.filterNot(_.owner.isUs))(_.heart.tileDistanceFast(With.geography.home)))
        .map(_.heart)
      lazy val distanceUs     = With.geography.home.tileDistanceSquared(tileBuilding)
      lazy val distanceEnemy  = tileComparison.map(_.tileDistanceSquared(tileBuilding)).getOrElse(0)
      lazy val closerToUs     = distanceUs < distanceEnemy
      
      scaryBuilding && closerToUs
    })
  }
}
