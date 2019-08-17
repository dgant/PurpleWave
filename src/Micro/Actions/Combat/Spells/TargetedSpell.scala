package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Heuristics.{SpellTargetAOE, SpellTargetSingle}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

abstract class TargetedSpell extends Action {
  
  protected def casterClass     : UnitClass
  protected def tech            : Tech
  protected def aoe             : Boolean
  protected def castRangeTiles  : Int
  protected def thresholdValue  : Double
  protected def lookaheadFrames : Int = With.latency.latencyFrames
  
  protected def valueTarget(target: UnitInfo): Double
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(casterClass)            &&
    With.self.hasTech(tech)         &&
    hasEnoughEnergy(unit)           &&
    additionalConditions(unit)      &&
    unit.matchups.enemies.nonEmpty
  }
  
  protected def hasEnoughEnergy(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > tech.energyCost
  }
  
  protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = true
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val framesToLive    = unit.matchups.framesToLive
    val framesOfSafety  = unit.matchups.framesOfSafety
    val safeDistance    = PurpleMath.clamp(framesToLive * unit.topSpeed, 0.0, 32.0 * 12.0)
    val totalRange      = safeDistance + 32.0 * castRangeTiles
    
    if (aoe) {
      val targetPixel = SpellTargetAOE.chooseTargetPixel(unit, totalRange, thresholdValue, valueTarget, lookaheadFrames)
      targetPixel.foreach(With.commander.useTechOnPixel(unit, tech, _))
      targetPixel.foreach(onCast(unit, _))
    }
    else {
      val targetUnit = SpellTargetSingle.chooseTarget(unit, totalRange, thresholdValue, valueTarget)
      targetUnit.foreach(With.commander.useTechOnUnit(unit, tech, _))
    }
  }
  
  // Event handler for when the unit issues a cast
  protected def onCast(caster: FriendlyUnitInfo, target: Pixel) {}
}
