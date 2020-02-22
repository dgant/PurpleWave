package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object DisruptionWeb extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.Corsair
  override protected def tech           : Tech      = Protoss.DisruptionWeb
  override protected def aoe            : Boolean   = true
  override protected def castRangeTiles : Int       = 9
  override protected def thresholdValue : Double    = casterClass.subjectiveValue / 2.0
  
  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if (target.underDisruptionWeb)  return 0.0
    if (target.flying)              return 0.0
    if ( ! target.canAttack)        return 0.0
    
    val output = (
      target.subjectiveValue *
      Math.min(1.0, target.matchups.targets.size          / 3.0)  *
      Math.min(1.0, target.matchups.framesToLive  / 72.0) *
      (if (target.moving) 0.5 else 1.0) *
      (if (target.isEnemy) 1.0 else -2.0)
    )
    
    output
  }
}
