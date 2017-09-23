package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.{TargetAOE, TargetSingle}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

abstract class TargetedSpell extends Action {
  
  protected def casterClass     : UnitClass
  protected def tech            : Tech
  protected def aoe             : Boolean
  protected def castRangeTiles  : Int
  protected def thresholdValue  : Double
  
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
    val framesToReact   = unit.matchups.framesToLiveDiffused - With.reaction.agencyAverage
    val framesOfSafety  = unit.matchups.framesOfSafetyDiffused
    val safeDistance    = Math.max(0, Math.min(framesToReact, framesOfSafety)) * unit.unitClass.topSpeed
    val totalRange      = safeDistance + 32.0 * castRangeTiles
    
    if (aoe) {
      val targetPixel = TargetAOE.chooseTarget(unit, totalRange, thresholdValue, valueTarget)
      targetPixel.foreach(With.commander.useTechOnPixel(unit, tech, _))
    }
    else {
      val targetUnit = TargetSingle.chooseTarget(unit, totalRange, thresholdValue, valueTarget)
      targetUnit.foreach(With.commander.useTechOnUnit(unit, tech, _))
    }
  }
}
