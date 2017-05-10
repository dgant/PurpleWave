package Information.Battles.Estimation

import Information.Battles.BattleTypes.{Battle, BattleGroup}
import Information.Battles.TacticsTypes.{Tactics, TacticsDefault, TacticsOptions}
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
  //TODO: Account for terrain positioning
  //TODO: Account for high ground
  
  var vulnerabilityGroundConcussive   = 0.0
  var vulnerabilityGroundExplosive    = 0.0
  var vulnerabilityGroundNormal       = 0.0
  var vulnerabilityAirConcussive      = 0.0
  var vulnerabilityAirExplosive       = 0.0
  var vulnerabilityAirNormal          = 0.0
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
  var attacksGround                 = 0.0
  var attacksAir                    = 0.0
  var subjectiveValue               = 0.0
  var subjectiveValueCostPerFrame   = 0.0
  var totalHealth                   = 0.0
  var totalFlyers                   = 0.0
  var totalUnits                    = 0.0
  var speedPixelsPerFrame           = 0.0
  var rangePixelsAir                = 0.0
  var rangePixelsGround             = 0.0
  var pixelsFromEnemy               = 0.0
  
  def this(
    unit              : UnitInfo,
    tactics           : TacticsOptions      = TacticsDefault.get,
    battle            : Option[Battle]      = None,
    battleGroup       : Option[BattleGroup] = None,
    considerGeometry  : Boolean) {
    
    this()
    
    val fighting      = isFighter(unit, tactics)
    val fleeing       = isFleer(unit, tactics)
    val fightingBonus = if (fighting) 1.0 else 0.0
    val fleeingBonus  = if (fleeing)  0.2 else 1.0
    val costPerFrame  = if (unit.unitClass.isWorker && (fighting || fleeing)) With.configuration.battleWorkerCostPerFrame else 0.0
    
    vulnerabilityGroundConcussive   = fleeingBonus * (if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size))
    vulnerabilityGroundExplosive    = fleeingBonus * (if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size))
    vulnerabilityGroundNormal       = fleeingBonus * (if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size))
    vulnerabilityAirConcussive      = fleeingBonus * (if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size))
    vulnerabilityAirExplosive       = fleeingBonus * (if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size))
    vulnerabilityAirNormal          = fleeingBonus * (if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size))
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
    attacksGround                   = if (unit.attacksGround) 1.0 else 0.0
    attacksAir                      = if (unit.attacksAir)    1.0 else 0.0
    subjectiveValue                 = unit.unitClass.subjectiveValue
    subjectiveValueCostPerFrame     = costPerFrame
    totalHealth                     = unit.totalHealth
    totalFlyers                     = if (unit.flying) 1.0 else 0.0
    totalUnits                      = 1.0
    speedPixelsPerFrame             = unit.topSpeed
    rangePixelsAir                  = unit.pixelRangeAir
    rangePixelsGround               = unit.pixelRangeGround
    pixelsFromEnemy =
      if (considerGeometry)
        battleGroup
          .map(group =>
            Math.max(
              0.0,
              unit.pixelDistanceFast(group.opponent.vanguard) - unit.pixelRangeMax))
          .sum
      else
        With.configuration.battleMarginPixels
  }
  
  def add(that:BattleEstimationUnit) {
    vulnerabilityGroundConcussive     += that.vulnerabilityGroundConcussive
    vulnerabilityGroundExplosive      += that.vulnerabilityGroundExplosive
    vulnerabilityGroundNormal         += that.vulnerabilityGroundNormal
    vulnerabilityAirConcussive        += that.vulnerabilityAirConcussive
    vulnerabilityAirExplosive         += that.vulnerabilityAirExplosive
    vulnerabilityAirNormal            += that.vulnerabilityAirNormal
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
    attacksGround                   += that.attacksGround
    attacksAir                      += that.attacksAir
    subjectiveValue                 += that.subjectiveValue
    subjectiveValueCostPerFrame     += that.subjectiveValueCostPerFrame
    totalHealth                     += that.totalHealth
    totalFlyers                     += that.totalFlyers
    totalUnits                      += that.totalUnits
    speedPixelsPerFrame             += that.speedPixelsPerFrame
    rangePixelsAir                  += that.rangePixelsAir
    rangePixelsGround               += that.rangePixelsGround
    pixelsFromEnemy                 += that.pixelsFromEnemy
  }
  
  def remove(that:BattleEstimationUnit) {
    vulnerabilityGroundConcussive     -= that.vulnerabilityGroundConcussive
    vulnerabilityGroundExplosive      -= that.vulnerabilityGroundExplosive
    vulnerabilityGroundNormal         -= that.vulnerabilityGroundNormal
    vulnerabilityAirConcussive        -= that.vulnerabilityAirConcussive
    vulnerabilityAirExplosive         -= that.vulnerabilityAirExplosive
    vulnerabilityAirNormal            -= that.vulnerabilityAirNormal
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
    attacksGround                   -= that.attacksGround
    attacksAir                      -= that.attacksAir
    subjectiveValue                 -= that.subjectiveValue
    subjectiveValueCostPerFrame     -= that.subjectiveValueCostPerFrame
    totalHealth                     -= that.totalHealth
    totalFlyers                     -= that.totalFlyers
    totalUnits                      -= that.totalUnits
    speedPixelsPerFrame             -= that.speedPixelsPerFrame
    rangePixelsAir                  -= that.rangePixelsAir
    rangePixelsGround               -= that.rangePixelsGround
    pixelsFromEnemy                 -= that.pixelsFromEnemy
  }
  
  private def unfocusedPenalty(unit:UnitInfo):Double = if (unit.attacksAir && unit.attacksGround) 0.0 else 1.0
  
  //Hacky way to implement the "fight with half of all workers" tactic
  private var acceptedLastWorker:Boolean = false
  
  private def isFighter(unit:UnitInfo, tactics:TacticsOptions):Boolean = {
    if ( ! unit.aliveAndComplete)
      false
    else if (unit.unitClass.isWorker) {
      if (tactics.has(Tactics.Workers.FightAll) && unit.unitClass.isWorker)
        true
      else if (tactics.has(Tactics.Workers.FightHalf) && unit.unitClass.isWorker) {
        acceptedLastWorker = ! acceptedLastWorker
        acceptedLastWorker
      }
      else false
    }
    else if (tactics.has(Tactics.Movement.Flee))
      false
    else if (tactics.has(Tactics.Wounded.Flee) && unit.wounded)
      false
    else
      unit.unitClass.helpsInCombat
  }
  
  private def isFleer(unit:UnitInfo, tactics:TacticsOptions):Boolean = {
    unit.canMoveThisFrame &&
      ! isFighter(unit, tactics) &&
      ( ! unit.unitClass.isWorker || tactics.has(Tactics.Workers.Flee))
  }
}
