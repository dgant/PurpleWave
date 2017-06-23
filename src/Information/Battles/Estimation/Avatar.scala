package Information.Battles.Estimation

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Engine.Damage
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class Avatar {
  
  var vulnerabilityGroundConcussive   = 0.0
  var vulnerabilityGroundExplosive    = 0.0
  var vulnerabilityGroundNormal       = 0.0
  var vulnerabilityAirConcussive      = 0.0
  var vulnerabilityAirExplosive       = 0.0
  var vulnerabilityAirNormal          = 0.0
  var dpfGroundConcussiveFocused      = 0.0
  var dpfGroundExplosiveFocused       = 0.0
  var dpfGroundNormalFocused          = 0.0
  var dpfAirConcussiveFocused         = 0.0
  var dpfAirExplosiveFocused          = 0.0
  var dpfAirNormalFocused             = 0.0
  var dpfGroundConcussiveUnfocused    = 0.0
  var dpfGroundExplosiveUnfocused     = 0.0
  var dpfGroundNormalUnfocused        = 0.0
  var dpfAirConcussiveUnfocused       = 0.0
  var dpfAirExplosiveUnfocused        = 0.0
  var dpfAirNormalUnfocused           = 0.0
  var attacksGround                   = 0.0
  var attacksAir                      = 0.0
  var subjectiveValue                 = 0.0
  var totalHealth                     = 0.0
  var totalFlyers                     = 0.0
  var totalUnits                      = 0.0
  
  def this(
    unit         : UnitInfo,
    nearestEnemy : Option[Pixel] = None) {
    
    this()
    
    val pixelsAway    = if (nearestEnemy.isDefined) unit.pixelDistanceFast(nearestEnemy.get) else With.configuration.battleMarginPixels
    val framesAway    = PurpleMath.nanToInfinity(Math.max(0.0, pixelsAway - unit.pixelRangeMax) / unit.topSpeed)
    val framesTotal   = With.configuration.battleEstimationFrames
    val effectiveness = Math.max(0.0, (framesTotal - framesAway) / framesTotal)
    
    vulnerabilityGroundConcussive   = if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size)
    vulnerabilityGroundExplosive    = if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size)
    vulnerabilityGroundNormal       = if (   unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size)
    vulnerabilityAirConcussive      = if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Concussive, unit.unitClass.size)
    vulnerabilityAirExplosive       = if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Explosive,  unit.unitClass.size)
    vulnerabilityAirNormal          = if ( ! unit.flying) 0.0 else Damage.scaleBySize(DamageType.Normal,     unit.unitClass.size)
    dpfGroundConcussiveFocused      = effectiveness * (if (unit.unitClass.groundDamageType == DamageType.Concussive) unit.damageOnHitBeforeArmorGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfGroundExplosiveFocused       = effectiveness * (if (unit.unitClass.groundDamageType == DamageType.Explosive)  unit.damageOnHitBeforeArmorGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfGroundNormalFocused          = effectiveness * (if (unit.unitClass.groundDamageType == DamageType.Normal)     unit.damageOnHitBeforeArmorGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfAirConcussiveFocused         = effectiveness * (if (unit.unitClass.airDamageType    == DamageType.Concussive) unit.damageOnHitBeforeArmorAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfAirExplosiveFocused          = effectiveness * (if (unit.unitClass.airDamageType    == DamageType.Explosive)  unit.damageOnHitBeforeArmorAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfAirNormalFocused             = effectiveness * (if (unit.unitClass.airDamageType    == DamageType.Normal)     unit.damageOnHitBeforeArmorAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfGroundConcussiveUnfocused    = dpfGroundConcussiveFocused  * unfocusedPenalty(unit)
    dpfGroundExplosiveUnfocused     = dpfGroundExplosiveFocused   * unfocusedPenalty(unit)
    dpfGroundNormalUnfocused        = dpfGroundNormalFocused      * unfocusedPenalty(unit)
    dpfAirConcussiveUnfocused       = dpfAirConcussiveFocused     * unfocusedPenalty(unit)
    dpfAirExplosiveUnfocused        = dpfAirExplosiveFocused      * unfocusedPenalty(unit)
    dpfAirNormalUnfocused           = dpfAirNormalFocused         * unfocusedPenalty(unit)
    attacksGround                   = if (unit.attacksGround) 1.0 else 0.0
    attacksAir                      = if (unit.attacksAir)    1.0 else 0.0
    subjectiveValue                 = unit.unitClass.subjectiveValue
    totalHealth                     = unit.totalHealth
    totalFlyers                     = if (unit.flying) 1.0 else 0.0
    totalUnits                      = 1.0
  }
  
  def add(that: Avatar) {
    vulnerabilityGroundConcussive   += that.vulnerabilityGroundConcussive
    vulnerabilityGroundExplosive    += that.vulnerabilityGroundExplosive
    vulnerabilityGroundNormal       += that.vulnerabilityGroundNormal
    vulnerabilityAirConcussive      += that.vulnerabilityAirConcussive
    vulnerabilityAirExplosive       += that.vulnerabilityAirExplosive
    vulnerabilityAirNormal          += that.vulnerabilityAirNormal
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
    totalHealth                     += that.totalHealth
    totalFlyers                     += that.totalFlyers
    totalUnits                      += that.totalUnits
  }
  
  def remove(that: Avatar) {
    vulnerabilityGroundConcussive   -= that.vulnerabilityGroundConcussive
    vulnerabilityGroundExplosive    -= that.vulnerabilityGroundExplosive
    vulnerabilityGroundNormal       -= that.vulnerabilityGroundNormal
    vulnerabilityAirConcussive      -= that.vulnerabilityAirConcussive
    vulnerabilityAirExplosive       -= that.vulnerabilityAirExplosive
    vulnerabilityAirNormal          -= that.vulnerabilityAirNormal
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
    totalHealth                     -= that.totalHealth
    totalFlyers                     -= that.totalFlyers
    totalUnits                      -= that.totalUnits
  }
  
  private def unfocusedPenalty(unit: UnitInfo): Double = {
    if (unit.attacksAir && unit.attacksGround) 0.0 else 1.0
  }
}
