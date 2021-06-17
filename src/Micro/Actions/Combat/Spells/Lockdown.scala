package Micro.Actions.Combat.Spells

import Mathematics.Maff
import ProxyBwapi.Races.Terran
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo._


object Lockdown extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Terran.Ghost
  override protected def tech           : Tech      = Terran.Lockdown
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 8
  override protected def thresholdValue : Double    = Terran.Ghost.subjectiveValue
  
  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if ( ! target.isEnemy)                return 0.0
    if ( ! target.unitClass.isMechanical) return 0.0
    if (target.unitClass.isBuilding)      return 0.0
    if (target.lockedDown)                return 0.0
    
    val thresholdLifetimeFrames = 24 * 5.0
    val cappedLifetime          = Math.max(thresholdLifetimeFrames, target.matchups.framesToLive)
    val fractionalValue         = cappedLifetime / thresholdLifetimeFrames
    val targetValueAbsolute     = target.subjectiveValue * fractionalValue
    val targetValueNow          = target.matchups.vpfDealingInRange * cappedLifetime
    val targetValueDetecting    = if(target.unitClass.isDetector) Maff.max(target.matchups.enemies.filter(_.cloaked).map(_.subjectiveValue.toDouble)).getOrElse(0.0) else 0.0
    val values                  = Vector(targetValueAbsolute, targetValueNow, targetValueDetecting)
    val output: Double          = values.max
    output
  }
}
