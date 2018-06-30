package Planning.UnitMatchers

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

abstract class UnitMatchAnyProxy extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = {
    if (unit.isFriendly) return false
    if ( ! unit.unitClass.isBuilding) return false
    if (unit.flying) return false
    
    val basesEnemy        = With.geography.enemyBases.map(_.heart.pixelCenter)
    val basesFriendly     = With.geography.ourBases.map(_.heart.pixelCenter)
    val distanceEnemy     = ByOption.min(basesEnemy     .map(unit.pixelDistanceCenter)).getOrElse(unit.pixelDistanceCenter(With.intelligence.mostBaselikeEnemyTile.pixelCenter))
    val distanceFriendly  = ByOption.min(basesFriendly  .map(unit.pixelDistanceCenter)).getOrElse(unit.pixelDistanceCenter(With.geography.home.pixelCenter))
    
    distanceFriendly < distanceEnemy * 1.25
  }
  
  val distanceRatio: Double
}

object UnitMatchProxied extends UnitMatchAnyProxy {
  
  override val distanceRatio = 1.25
  
}

object UnitMatchProxiedInOurBase extends UnitMatchAnyProxy {
  
  override val distanceRatio = 0.7
  
}