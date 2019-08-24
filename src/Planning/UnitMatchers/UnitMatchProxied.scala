package Planning.UnitMatchers

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo

abstract class UnitMatchAnyProxy extends UnitMatcher {

  private def baseDistance(unit: UnitInfo, player: PlayerInfo): Double = {
    val home = With.geography.startBases
      .find(_.owner == player)
      .orElse(With.geography.startBases.find(_.owner.isNeutral))
      .map(_.heart)
      .getOrElse(if (player.isUs) With.geography.home else With.intelligence.mostBaselikeEnemyTile)
      .pixelCenter
    val output = unit.pixelDistanceTravelling(home)
    output
  }
  override def accept(unit: UnitInfo): Boolean = {
    if (unit.isFriendly) return false
    if ( ! unit.unitClass.isBuilding) return false
    if (unit.flying) return false
    if (unit.base.exists(b => b.isStartLocation && ! b.isOurMain)) return false // Hack fix to detecting normal buildings as proxied
    if (unit.base.exists(b => b.isNaturalOf.exists( ! _.isOurMain))) return false // Hack fix to detecting normal buildings as proxied

    val distanceEnemy     = baseDistance(unit, unit.player)
    val distanceFriendly  = baseDistance(unit, With.self)
    
    distanceFriendly < distanceEnemy * 1.4
  }
  
  val distanceRatio: Double
}

object UnitMatchProxied extends UnitMatchAnyProxy {
  
  override val distanceRatio = 1.25
  
}

object UnitMatchProxiedInOurBase extends UnitMatchAnyProxy {
  
  override val distanceRatio = 0.7
  
}