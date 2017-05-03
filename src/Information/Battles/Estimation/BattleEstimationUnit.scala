package Information.Battles.Estimation

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
  var totalHealth                   = 0.0
  var totalFlyers                   = 0.0
  var totalUnits                    = 0.0
  
  def this(unit:UnitInfo) {
    this()
    damageScaleGroundConcussive     = if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size)
    damageScaleGroundExplosive      = if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size)
    damageScaleGroundNormal         = if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size)
    damageScaleAirConcussive        = if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size)
    damageScaleAirExplosive         = if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size)
    damageScaleAirNormal            = if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size)
    dpfGroundConcussiveFocused      = (if (unit.unitClass.groundDamageTypeRaw == DamageType.Concussive) unit.damageOnHitBeforeArmorGround else 0.0) / unit.coolDownMaxGround.toDouble
    dpfGroundExplosiveFocused       = (if (unit.unitClass.groundDamageTypeRaw == DamageType.Explosive)  unit.damageOnHitBeforeArmorGround else 0.0) / unit.coolDownMaxGround.toDouble
    dpfGroundNormalFocused          = (if (unit.unitClass.groundDamageTypeRaw == DamageType.Normal)     unit.damageOnHitBeforeArmorGround else 0.0) / unit.coolDownMaxGround.toDouble
    dpfAirConcussiveFocused         = (if (unit.unitClass.airDamageTypeRaw    == DamageType.Concussive) unit.damageOnHitBeforeArmorAir    else 0.0) / unit.coolDownMaxAir.toDouble
    dpfAirExplosiveFocused          = (if (unit.unitClass.airDamageTypeRaw    == DamageType.Explosive)  unit.damageOnHitBeforeArmorAir    else 0.0) / unit.coolDownMaxAir.toDouble
    dpfAirNormalFocused             = (if (unit.unitClass.airDamageTypeRaw    == DamageType.Normal)     unit.damageOnHitBeforeArmorAir    else 0.0) / unit.coolDownMaxAir.toDouble
    dpfGroundConcussiveUnfocused    = dpfGroundConcussiveFocused  * unfocusedPenalty(unit:UnitInfo)
    dpfGroundExplosiveUnfocused     = dpfGroundExplosiveUnfocused * unfocusedPenalty(unit:UnitInfo)
    dpfGroundNormalUnfocused        = dpfGroundNormalUnfocused    * unfocusedPenalty(unit:UnitInfo)
    dpfAirConcussiveUnfocused       = dpfAirConcussiveUnfocused   * unfocusedPenalty(unit:UnitInfo)
    dpfAirExplosiveUnfocused        = dpfAirExplosiveUnfocused    * unfocusedPenalty(unit:UnitInfo)
    dpfAirNormalUnfocused           = dpfAirNormalUnfocused       * unfocusedPenalty(unit:UnitInfo)
    speedPixelsPerFrame             = unit.topSpeed
    rangePixelsAir                  = unit.pixelRangeAir
    rangePixelsGround               = unit.pixelRangeGround
    attacksGround                   = if (unit.attacksGround) 1.0 else 0.0
    attacksAir                      = if (unit.attacksAir)    1.0 else 0.0
    subjectiveValue                 = unit.unitClass.subjectiveValue
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
    totalHealth                     -= that.totalHealth
    totalFlyers                     -= that.totalFlyers
    totalUnits                      -= that.totalUnits
  }
  
  private def unfocusedPenalty(unit:UnitInfo):Double = if (unit.attacksAir && unit.attacksGround) 0.0 else 1.0
}
