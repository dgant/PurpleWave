package Planning.UnitMatchers

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo

abstract class MatchAnyProxy extends UnitMatcher {

  private def mainBases(player: PlayerInfo): Iterable[Base] = {
    if (player.isUs) return Seq(With.geography.ourMain)
    val bases = Maff.orElse(
      With.geography.startBases.filter(_.owner == player),
      With.geography.startBases.filter(base => base.owner.isNeutral && base.lastScoutedFrame == 0),
      With.geography.startBases.filter(base => base.owner.isNeutral),
      With.geography.bases.filter( ! _.owner.isUs),
      With.geography.bases)
    bases
  }

  private def baseDistance(unit: UnitInfo, player: PlayerInfo): Double = {
    val bases = mainBases(player)
    val output = bases.map(base => unit.pixelDistanceTravelling(base.townHallTile)).min
    output
  }

  override def apply(unit: UnitInfo): Boolean = {
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

object MatchProxied extends MatchAnyProxy {

  override val distanceRatio = 1.25

}

object MatchProxiedInOurBase extends MatchAnyProxy {
  
  override val distanceRatio = 0.7
  
}