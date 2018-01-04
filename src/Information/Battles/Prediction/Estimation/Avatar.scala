package Information.Battles.Prediction.Estimation

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.Engine.Damage
import ProxyBwapi.UnitInfo.UnitInfo

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
    unit          : UnitInfo,
    nearestEnemy  : Option[Pixel] = None,
    attacking     : Boolean       = false,
    retreating    : Boolean       = false,
    chasing       : Boolean       = false) {
    
    this()
  
    val splashFactor = MicroValue.maxSplashFactor(unit)
    
    val geometric     = nearestEnemy.isDefined
    val contributes   = unit.unitClass.helpsInCombat
    val range         = unit.pixelRangeMax + 32.0 * (if (attacking || ! unit.canMove) 1.0 else 3.0)
    val pixelsAway    = if (geometric) unit.pixelDistanceFast(nearestEnemy.get) else With.configuration.avatarBattleDistancePixels
    val framesAway    = if (pixelsAway <= range) 0.0 else if (chasing) Double.PositiveInfinity else PurpleMath.nanToInfinity(Math.max(0.0, pixelsAway - range) / unit.topSpeed * 0.5)
    val framesTotal   = With.configuration.battleEstimationFrames
    var efficacy      = if (retreating) 0.0 else splashFactor * Math.max(0.0, (framesTotal - framesAway) / framesTotal)
    val altitudeBonus = if (unit.flying || ! geometric) 1.0 else With.grids.altitudeBonus.get(unit.tileIncludingCenter)
    var fortitude     = altitudeBonus * (if (geometric && unit.effectivelyCloaked) 5.0 else 1.0)

    // Very rough approximation -- of course Dark Swarm matters when it's the *target* under the swarm
    if (unit.underDisruptionWeb || (unit.underDarkSwarm && unit.unitClass.unaffectedByDarkSwarm)) {
      efficacy = 0.0
    }
    
    if (unit.canStim && ! unit.stimmed) {
      efficacy *= 1.75
    }
    
    vulnerabilityGroundConcussive   = if (   unit.flying) 0.0 else Damage.scaleBySize(Damage.Concussive, unit.unitClass.size)
    vulnerabilityGroundExplosive    = if (   unit.flying) 0.0 else Damage.scaleBySize(Damage.Explosive,  unit.unitClass.size)
    vulnerabilityGroundNormal       = if (   unit.flying) 0.0 else Damage.scaleBySize(Damage.Normal,     unit.unitClass.size)
    vulnerabilityAirConcussive      = if ( ! unit.flying) 0.0 else Damage.scaleBySize(Damage.Concussive, unit.unitClass.size)
    vulnerabilityAirExplosive       = if ( ! unit.flying) 0.0 else Damage.scaleBySize(Damage.Explosive,  unit.unitClass.size)
    vulnerabilityAirNormal          = if ( ! unit.flying) 0.0 else Damage.scaleBySize(Damage.Normal,     unit.unitClass.size)
    dpfGroundConcussiveFocused      = efficacy * (if (unit.unitClass.groundDamageType == Damage.Concussive) unit.damageOnHitGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfGroundExplosiveFocused       = efficacy * (if (unit.unitClass.groundDamageType == Damage.Explosive)  unit.damageOnHitGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfGroundNormalFocused          = efficacy * (if (unit.unitClass.groundDamageType == Damage.Normal)     unit.damageOnHitGround else 0.0) / unit.cooldownMaxGround.toDouble
    dpfAirConcussiveFocused         = efficacy * (if (unit.unitClass.airDamageType    == Damage.Concussive) unit.damageOnHitAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfAirExplosiveFocused          = efficacy * (if (unit.unitClass.airDamageType    == Damage.Explosive)  unit.damageOnHitAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfAirNormalFocused             = efficacy * (if (unit.unitClass.airDamageType    == Damage.Normal)     unit.damageOnHitAir    else 0.0) / unit.cooldownMaxAir.toDouble
    dpfGroundConcussiveUnfocused    = dpfGroundConcussiveFocused  * unfocusedPenalty(unit)
    dpfGroundExplosiveUnfocused     = dpfGroundExplosiveFocused   * unfocusedPenalty(unit)
    dpfGroundNormalUnfocused        = dpfGroundNormalFocused      * unfocusedPenalty(unit)
    dpfAirConcussiveUnfocused       = dpfAirConcussiveFocused     * unfocusedPenalty(unit)
    dpfAirExplosiveUnfocused        = dpfAirExplosiveFocused      * unfocusedPenalty(unit)
    dpfAirNormalUnfocused           = dpfAirNormalFocused         * unfocusedPenalty(unit)
    attacksGround                   = if (unit.unitClass.attacksGround) 1.0 else 0.0
    attacksAir                      = if (unit.unitClass.attacksAir)    1.0 else 0.0
    subjectiveValue                 = unit.subjectiveValue
    totalHealth                     = fortitude * unit.totalHealth
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
    if (unit.unitClass.attacksAir && unit.unitClass.attacksGround) 0.0 else 1.0
  }
}
