package Micro.Actions.Combat.Spells

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends TargetedSpell {
  
  override protected def casterClass      : UnitClass = Protoss.HighTemplar
  override protected def tech             : Tech      = Protoss.PsionicStorm
  override protected def aoe              : Boolean   = true
  override protected def castRangeTiles   : Int       = 9
  override protected def thresholdValue   : Double    = 1.5 * Terran.SiegeTankUnsieged.logSubjectiveValue
  override protected def lookaheadFrames  : Int       = With.latency.latencyFrames

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if (With.grids.psionicStorm.isSet(target.tileIncludingCenter)) return 0.0
    if (target.unitClass.isBuilding)    return 0.0
    if (target.underStorm)              return 0.0
    if (target.stasised)                return 0.0
    if (target.invincible)              return 0.0
    if (target.isAny(
      Protoss.Interceptor,
      Zerg.Larva,
      Zerg.Egg,
      Zerg.LurkerEgg)) return 0.0

    // Don't wander into tank range
    if (target.isEnemy
      && target.is(Terran.SiegeTankSieged)
      && caster.pixelDistanceCenter(target) > 32 * castRangeTiles
      && (caster.visibleToOpponents || target.tileIncludingCenter.altitudeBonus >= caster.tileIncludingCenter.altitudeBonus)
      && target.matchups.targetsInRange.isEmpty) {
      return 0.0
    }

    val multiplayerPlayer = if (target.isEnemy) 1.0 else if (target.isFriendly) -2.0 else 0.0
    val multiplierUnit    = target.unitClass.logSubjectiveValue
    val multiplierDanger  = if (caster.matchups.threatsInRange.nonEmpty) 1.25 else 1.0
    val output = multiplayerPlayer * multiplierUnit * multiplierDanger
    output
  }
  
  override def onCast(caster: FriendlyUnitInfo, target: Pixel) {
    With.grids.psionicStorm.addPsionicStorm(target)
    With.animations.addMap(() => DrawMap.drawStar(target, 10, Colors.NeonRed))
  }
}
