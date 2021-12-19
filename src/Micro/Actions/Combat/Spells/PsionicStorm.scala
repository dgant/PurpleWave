package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends TargetedSpell {
  
  override protected def casterClass      : UnitClass = Protoss.HighTemplar
  override protected def tech             : Tech      = Protoss.PsionicStorm
  override protected def aoe              : Boolean   = true
  override protected def castRangeTiles   : Int       = 9
  override protected def thresholdValue   : Double    = 12 * Terran.Marine.subjectiveValue
  override protected def lookaheadPixels  : Int       = 24
  override protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = unit.agent.shouldEngage || unit.matchups.threatsInRange.nonEmpty || unit.base.exists(_.owner.isEnemy)

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if (With.grids.psionicStorm.isSet(target.tile)) return 0.0
    if ( ! target.unitClass.canBeStormed) return 0.0
    if (target.underStorm)  return 0.0
    if (target.stasised)    return 0.0
    if (target.invincible)  return 0.0
    if ( ! target.visible)  return 0.0
    if (target.isAny(
      Protoss.Interceptor,
      Zerg.Larva,
      Zerg.Egg,
      Zerg.LurkerEgg)) return 0.0

    val templar = caster.matchups.allyTemplarCount()
    val storms  = caster.energy / Protoss.PsionicStorm.energyCost

    // Don't wander into tank range
    if (target.isEnemy
      && ! caster.agent.shouldEngage
      && templar < 3
      && Terran.SiegeTankSieged(target)
      && caster.pixelDistanceCenter(target) > 32 * castRangeTiles
      && (caster.visibleToOpponents || target.tile.altitude >= caster.tile.altitude)
      && target.matchups.targetsInRange.isEmpty
      && ! caster.effectivelyCloaked
      && target.cooldownLeft < Maff.nanToInfinity((caster.pixelDistanceEdge(target) - 32 * castRangeTiles) / caster.topSpeed)) {
      return 0.0
    }

    val multiplayerPlayer = if (target.isEnemy) 1.0 else if (target.isFriendly) -2.0 else 0.0
    val multiplierUnit    = 3 * Terran.Marine.subjectiveValue + Math.min(target.unitClass.subjectiveValue, Terran.SiegeTankUnsieged.subjectiveValue)
    val multiplierDanger  = if (caster.matchups.threatsInRange.nonEmpty) 1.25 else 1.0
    val multiplierMany    = if (templar > 5) 2.0 else if (templar > 2) 1.4 else 1.0
    val multiplierRich    = if (storms >= 3) 2.0 else if (storms >= 2) 1.5 else 1.0
    val multiplierSpeed   = Maff.clamp(Maff.nanToOne(Protoss.Dragoon.topSpeed / target.topSpeed), 0, 1)
    val multiplierFight   = if (caster.agent.shouldEngage || caster.matchups.pixelsOfEntanglement >= 0) 1.0 else 0.25
    val multiplierSilly =
      if (target.isAny(Terran.ScienceVessel, Protoss.Observer, Zerg.Overlord))
        0.2
      else if (target.isAny(Terran.Vulture, Zerg.Mutalisk, Protoss.Dragoon) && target.matchups.targetsInRange.isEmpty && target.matchups.threatsInRange.isEmpty)
        0.2
      else
        1.0
    val output            = multiplayerPlayer * multiplierUnit * multiplierDanger * multiplierMany * multiplierRich * multiplierSpeed * multiplierFight * multiplierSilly
    output
  }
  
  override def onCast(caster: FriendlyUnitInfo, target: Pixel) {
    With.grids.psionicStorm.addPsionicStorm(target)
    //With.animations.addMap(() => DrawMap.drawStar(target, 10, Colors.NeonRed))
  }
}
