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
  override protected def lookaheadPixels  : Int       = 12
  override protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = unit.agent.shouldEngage || unit.matchups.threatsInRange.nonEmpty || unit.base.exists(_.owner.isEnemy)

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if (With.grids.psionicStorm.isSet(target.tile)) return 0.0
    if ( ! target.unitClass.canBeStormed) return 0.0
    if (target.underStorm)  return 0.0
    if (target.stasised)    return 0.0
    if (target.invincible)  return 0.0
    if ( ! target.visible)  return 0.0
    if (target.isFriendly && target.isTransport && target.loadedUnitCount == 0) return 0.0 // Dumb hack to ensure our storm drops don't try too hard to avoid the shuttle
    if (target.isAny(Protoss.Interceptor, Zerg.Larva, Zerg.Egg, Zerg.LurkerEgg)) return 0.0

    val multiplierConfidence  = Math.max(1.0, Maff.nanToOne(1 / (1 + caster.confidence11)))
    val multiplierPlayer      = if (target.isEnemy) 1.0 else if (target.isFriendly) -2.0 else 0.0
    val multiplierUnit        = 3 * Terran.Marine.subjectiveValue + Math.min(target.unitClass.subjectiveValue, Terran.SiegeTankUnsieged.subjectiveValue)
    val multiplierDanger      = if (caster.matchups.threatsInRange.nonEmpty) 2.0 else 1.0
    val multiplierRich        = 1.0 + 0.3 * caster.team.map(_.storms).getOrElse(0)
    val multiplierSpeed       = Maff.clamp(Maff.nanToOne(Protoss.Dragoon.topSpeed / target.topSpeed), 0, 1)
    val multiplierSilly =
      if (target.isAny(Terran.ScienceVessel, Protoss.Observer, Zerg.Overlord))
        0.1
      else if (target.isAny(Terran.Vulture, Zerg.Mutalisk, Protoss.Dragoon) && target.matchups.targetsInRange.isEmpty && target.matchups.threatsInRange.isEmpty)
        0.2
      else
        1.0
    val output = multiplierConfidence * multiplierPlayer * multiplierUnit * multiplierDanger * multiplierRich * multiplierSpeed * multiplierSilly
    output
  }
  
  override def onCast(caster: FriendlyUnitInfo, target: Pixel) {
    With.grids.psionicStorm.addPsionicStorm(target)
  }
}
