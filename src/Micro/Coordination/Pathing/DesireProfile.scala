package Micro.Coordination.Pathing

import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

case class DesireProfile(var home: Int, var safety: Int, var freedom: Int) {
  def this(unit: FriendlyUnitInfo) {
    this(0, 0, 0)
    def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.origin) - threat.pixelRangeAgainst(unit) * threat.topSpeed
    val distanceOriginUs    = unit.pixelDistanceTravelling(unit.agent.origin)
    val distanceOriginEnemy = ByOption.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) - t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
    val enemyCloser         = distanceOriginUs + 160 >= distanceOriginEnemy
    val timeOriginUs        = unit.framesToTravelTo(unit.agent.origin)
    val timeOriginEnemy     = TakeN.percentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).getOrElse(Double.PositiveInfinity)
    val enemySooner         = timeOriginUs + 96 >= timeOriginEnemy
    val enemySieging        = unit.matchups.enemies.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker)) && ! unit.base.exists(_.owner.isEnemy)
    val atHome              = unit.zone == unit.agent.origin.zone
    val scouting            = unit.agent.isScout
    val desireToGoHome      =
      if (unit.is(Protoss.DarkTemplar))
        -1
      else if (enemySieging && ! enemyCloser && ! enemySooner)
        -1
      else if (scouting || atHome)
        0
      else if (unit.base.exists(_.owner.isEnemy))
        2
      else
        ((if (enemyCloser) 1 else 0) + (if (enemySooner) 1 else 0))

    val desireForFreedom  = if (unit.flying && ! unit.matchups.threats.exists(_.unitClass.dealsRadialSplashDamage)) 0 else 1
    val desireForSafety   = PurpleMath.clamp(0, 3, (3 * (1 - unit.matchups.framesOfSafety / 72)).toInt)
    home = desireToGoHome
    safety = desireForSafety
    freedom = desireForFreedom
  }
}
