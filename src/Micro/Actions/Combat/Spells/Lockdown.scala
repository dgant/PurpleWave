package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.Terran
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo._

object Lockdown extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Terran.Ghost
  override protected def tech           : Tech      = Terran.Lockdown
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 8
  override protected def thresholdValue : Double    = Terran.Goliath.subjectiveValue
  
  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if ( ! target.isEnemy)                return 0.0
    if ( ! target.unitClass.isMechanical) return 0.0
    if (target.unitClass.isBuilding)      return 0.0
    if (target.lockedDown)                return 0.0
    
    val output = target.subjectiveValue / Math.max(1.0, caster.pixelDistanceEdge(target) - 32 * castRangeTiles - caster.matchups.pixelsOfSafety)
    output
  }
}
