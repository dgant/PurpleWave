package Planning.UnitMatchers

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo

abstract class UnitMatchAnyProxy extends UnitMatcher {

  private def mainBases(player: PlayerInfo): Seq[Base] = {
    if (player.isUs) return Seq(With.geography.ourMain)
    var bases = With.geography.startBases.filter(_.owner == player)
    if (bases.isEmpty) {
      bases = With.geography.startBases.filter(base => base.owner.isNeutral && base.lastScoutedFrame == 0)
    }
    if (bases.isEmpty) {
      bases = With.geography.startBases.filter(base => base.owner.isNeutral)
    }
    if (bases.isEmpty) {
      bases = With.geography.bases.filter( ! _.owner.isUs)
    }
    if (bases.isEmpty) {
      bases = With.geography.bases
    }
    bases
  }


  private def baseDistance(unit: UnitInfo, player: PlayerInfo): Double = {
    val bases = mainBases(player)
    val output = bases.map(base => unit.pixelDistanceTravelling(base.townHallTile)).min
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