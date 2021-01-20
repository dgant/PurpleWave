package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.Commander
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
  protected def lookaheadPixels : Int = 0
  protected def pixelWidth      : Int = 96
  protected def pixelHeight     : Int = 96
  
  protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(casterClass)            &&
    With.self.hasTech(tech)         &&
    hasEnoughEnergy(unit)           &&
    unit.matchups.enemies.nonEmpty  &&
    additionalConditions(unit)
  }
  
  protected def hasEnoughEnergy(unit: FriendlyUnitInfo): Boolean = {
    unit.energy >= tech.energyCost
  }
  
  protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = true
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val safeDistance  = Math.max(0, -unit.matchups.pixelsOfEntanglement)
    val totalRange    = safeDistance + 32.0 * castRangeTiles + 32 // The margin is just some encouragement
    
    if (aoe) {
      val targetPixel = new SpellTargetAOE().chooseTargetPixel(unit, totalRange, thresholdValue, valueTarget, pixelWidth = pixelWidth, pixelHeight = pixelHeight)
      targetPixel.foreach(Commander.useTechOnPixel(unit, tech, _))
      targetPixel.foreach(onCast(unit, _))
    }
    else {
      val targetUnit = SpellTargetSingle.chooseTarget(unit, totalRange, thresholdValue, valueTarget)
      targetUnit.foreach(Commander.useTechOnUnit(unit, tech, _))
    }
  }
  
  // Event handler for when the unit issues a cast
  protected def onCast(caster: FriendlyUnitInfo, target: Pixel) {}
}
