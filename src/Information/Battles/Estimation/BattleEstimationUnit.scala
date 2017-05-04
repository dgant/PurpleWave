package Information.Battles.Estimation

import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import ProxyBwapi.Engine.Damage
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class BattleEstimationUnit {
  
  //TODO: Account for shield/defensive matrix taking full dpf
  //TODO: Account for armor/shieldarmor
  //TODO: Account for splash dpf
  //TODO: Account for spells
  //TODO: Account for healing
  //TODO: Account for combat positioning
  //TODO: Account for terrain positioning
  //TODO: Account for high ground
  //TODO: Incorporate wounded-fleeing tactics
  
  var damageScaleGroundConcussive   = 0.0
  var damageScaleGroundExplosive    = 0.0
  var damageScaleGroundNormal       = 0.0
  var damageScaleAirConcussive      = 0.0
  var damageScaleAirExplosive       = 0.0
  var damageScaleAirNormal          = 0.0
  var dpfGroundConcussiveFocused    = 0.0
  var dpfGroundExplosiveFocused     = 0.0
  var dpfGroundNormalFocused        = 0.0
  var dpfAirConcussiveFocused       = 0.0
  var dpfAirExplosiveFocused        = 0.0
  var dpfAirNormalFocused           = 0.0
  var dpfGroundConcussiveUnfocused  = 0.0
  var dpfGroundExplosiveUnfocused   = 0.0
  var dpfGroundNormalUnfocused      = 0.0
  var dpfAirConcussiveUnfocused     = 0.0
  var dpfAirExplosiveUnfocused      = 0.0
  var dpfAirNormalUnfocused         = 0.0
  var speedPixelsPerFrame           = 0.0
  var rangePixelsAir                = 0.0
  var rangePixelsGround             = 0.0
  var attacksGround                 = 0.0
  var attacksAir                    = 0.0
  var subjectiveValue               = 0.0
  var subjectiveValueCostPerFrame   = 0.0
  var totalHealth                   = 0.0
  var totalFlyers                   = 0.0
  var totalUnits                    = 0.0
  
  def this(unit:UnitInfo, tactics:TacticsOptions) {
    this()
    
    val fighting      = isFighter(unit, tactics)
    val fleeing       = isFleer(unit, tactics)
    val fightingBonus = if (fighting) 1.0 else 0.0
    val fleeingBonus  = if (fleeing) 0.25 else 1.0
    val costPerFrame  = if (unit.unitClass.isWorker && (fighting || fleeing)) With.configuration.battleWorkerCostPerFrame else 0.0
    
    damageScaleGroundConcussive     = fleeingBonus * (if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size))
    damageScaleGroundExplosive      = fleeingBonus * (if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size))
    damageScaleGroundNormal         = fleeingBonus * (if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size))
    damageScaleAirConcussive        = fleeingBonus * (if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size))
    damageScaleAirExplosive         = fleeingBonus * (if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size))
    damageScaleAirNormal            = fleeingBonus * (if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size))
    dpfGroundConcussiveFocused      = fightingBonus * (if (unit.unitClass.groundDamageType == DamageType.Concussive) unit.damageOnHitBeforeArmorGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfGroundExplosiveFocused       = fightingBonus * (if (unit.unitClass.groundDamageType == DamageType.Explosive)  unit.damageOnHitBeforeArmorGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfGroundNormalFocused          = fightingBonus * (if (unit.unitClass.groundDamageType == DamageType.Normal)     unit.damageOnHitBeforeArmorGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfAirConcussiveFocused         = fightingBonus * (if (unit.unitClass.airDamageType    == DamageType.Concussive) unit.damageOnHitBeforeArmorAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfAirExplosiveFocused          = fightingBonus * (if (unit.unitClass.airDamageType    == DamageType.Explosive)  unit.damageOnHitBeforeArmorAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfAirNormalFocused             = fightingBonus * (if (unit.unitClass.airDamageType    == DamageType.Normal)     unit.damageOnHitBeforeArmorAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfGroundConcussiveUnfocused    = dpfGroundConcussiveFocused  * unfocusedPenalty(unit:UnitInfo)
    dpfGroundExplosiveUnfocused     = dpfGroundExplosiveFocused   * unfocusedPenalty(unit:UnitInfo)
    dpfGroundNormalUnfocused        = dpfGroundNormalFocused      * unfocusedPenalty(unit:UnitInfo)
    dpfAirConcussiveUnfocused       = dpfAirConcussiveFocused     * unfocusedPenalty(unit:UnitInfo)
    dpfAirExplosiveUnfocused        = dpfAirExplosiveFocused      * unfocusedPenalty(unit:UnitInfo)
    dpfAirNormalUnfocused           = dpfAirNormalFocused         * unfocusedPenalty(unit:UnitInfo)
    speedPixelsPerFrame             = unit.topSpeed
    rangePixelsAir                  = unit.pixelRangeAir
    rangePixelsGround               = unit.pixelRangeGround
    attacksGround                   = if (unit.attacksGround) 1.0 else 0.0
    attacksAir                      = if (unit.attacksAir)    1.0 else 0.0
    subjectiveValue                 = unit.unitClass.subjectiveValue
    subjectiveValueCostPerFrame     = costPerFrame
    totalHealth                     = unit.totalHealth
    totalFlyers                     = if (unit.flying) 1.0 else 0.0
    totalUnits                      = 1.0
  }
  
  def add(that:BattleEstimationUnit) {
    damageScaleGroundConcussive     += that.damageScaleGroundConcussive
    damageScaleGroundExplosive      += that.damageScaleGroundExplosive
    damageScaleGroundNormal         += that.damageScaleGroundNormal
    damageScaleAirConcussive        += that.damageScaleAirConcussive
    damageScaleAirExplosive         += that.damageScaleAirExplosive
    damageScaleAirNormal            += that.damageScaleAirNormal
    dpfGroundConcussiveFocused      += that.dpfGroundConcussiveFocused
    dpfGroundExplosiveFocused       += that.dpfGroundExplosiveFocused
    dpfGroundNormalFocused          += that.dpfGroundNormalFocused
    dpfAirConcussiveFocused         += that.dpfAirConcussiveFocused
    dpfAirExplosiveFocused          += that.dpfAirExplosiveFocused
    dpfAirNormalFocused             += that.dpfAirNormalFocused
    dpfGroundConcussiveUnfocused    += that.dpfGroundConcussiveUnfocused
    dpfGroundExplosiveUnfocused     += that.dpfGroundExplosiveUnfocused
    dpfGroundNormalUnfocused        += that.dpfGroundNormalUnfocused
    dpfAirConcussiveUnfocused       += that.dpfAirConcussiveUnfocused
    dpfAirExplosiveUnfocused        += that.dpfAirExplosiveUnfocused
    dpfAirNormalUnfocused           += that.dpfAirNormalUnfocused
    speedPixelsPerFrame             += that.speedPixelsPerFrame
    rangePixelsAir                  += that.rangePixelsAir
    rangePixelsGround               += that.rangePixelsGround
    attacksGround                   += that.attacksGround
    attacksAir                      += that.attacksAir
    subjectiveValue                 += that.subjectiveValue
    subjectiveValueCostPerFrame     += that.subjectiveValueCostPerFrame
    totalHealth                     += that.totalHealth
    totalFlyers                     += that.totalFlyers
    totalUnits                      += that.totalUnits
  }
  
  def remove(that:BattleEstimationUnit) {
    damageScaleGroundConcussive     -= that.damageScaleGroundConcussive
    damageScaleGroundExplosive      -= that.damageScaleGroundExplosive
    damageScaleGroundNormal         -= that.damageScaleGroundNormal
    damageScaleAirConcussive        -= that.damageScaleAirConcussive
    damageScaleAirExplosive         -= that.damageScaleAirExplosive
    damageScaleAirNormal            -= that.damageScaleAirNormal
    dpfGroundConcussiveFocused      -= that.dpfGroundConcussiveFocused
    dpfGroundExplosiveFocused       -= that.dpfGroundExplosiveFocused
    dpfGroundNormalFocused          -= that.dpfGroundNormalFocused
    dpfAirConcussiveFocused         -= that.dpfAirConcussiveFocused
    dpfAirExplosiveFocused          -= that.dpfAirExplosiveFocused
    dpfAirNormalFocused             -= that.dpfAirNormalFocused
    dpfGroundConcussiveUnfocused    -= that.dpfGroundConcussiveUnfocused
    dpfGroundExplosiveUnfocused     -= that.dpfGroundExplosiveUnfocused
    dpfGroundNormalUnfocused        -= that.dpfGroundNormalUnfocused
    dpfAirConcussiveUnfocused       -= that.dpfAirConcussiveUnfocused
    dpfAirExplosiveUnfocused        -= that.dpfAirExplosiveUnfocused
    dpfAirNormalUnfocused           -= that.dpfAirNormalUnfocused
    speedPixelsPerFrame             -= that.speedPixelsPerFrame
    rangePixelsAir                  -= that.rangePixelsAir
    rangePixelsGround               -= that.rangePixelsGround
    attacksGround                   -= that.attacksGround
    attacksAir                      -= that.attacksAir
    subjectiveValue                 -= that.subjectiveValue
    subjectiveValueCostPerFrame     -= that.subjectiveValueCostPerFrame
    totalHealth                     -= that.totalHealth
    totalFlyers                     -= that.totalFlyers
    totalUnits                      -= that.totalUnits
  }
  
  private def unfocusedPenalty(unit:UnitInfo):Double = if (unit.attacksAir && unit.attacksGround) 0.0 else 1.0
  
  //Hacky way to implement the "fight with half of all workers" tactic
  private var acceptNextWorker:Boolean = false
  
  private def isFighter(unit:UnitInfo, tactics:TacticsOptions):Boolean = {
    if ( ! unit.alive) return false
    if (unit.unitClass.isWorker) {
      if (tactics.has(Tactics.Workers.FightAll)) {
        return true
      }
      if (tactics.has(Tactics.Workers.FightHalf)) {
        acceptNextWorker = ! acceptNextWorker
        return acceptNextWorker
      }
      return false
    }
    if (unit.wounded && tactics.has(Tactics.Wounded.Flee)) {
      return false
    }
    if (tactics.has(Tactics.Movement.Flee)) {
      return false
    }
    unit.unitClass.helpsInCombat
  }
  
  private def isFleer(unit:UnitInfo, tactics:TacticsOptions):Boolean = {
    unit.canMoveThisFrame &&
      ! isFighter(unit, tactics) &&
      ( ! unit.unitClass.isWorker || tactics.has(Tactics.Workers.Flee))
  }
}
