package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object MindControl extends TargetedSpell {
  
  override protected def casterClass        : UnitClass = Protoss.DarkArchon
  override protected def tech               : Tech      = Protoss.MindControl
  override protected def aoe                : Boolean   = false
  override protected def castRangeTiles     : Int       = 8
  override protected def thresholdValue     : Double    = 0.5 * Protoss.DarkArchon.subjectiveValue
  override protected def bonusSearchPixels  : Int       = 96
  
  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if (target.unitClass.isBuilding)  return -1.0
    if (target.isFriendly)            return -1.0
    if (target.stasised)              return -1.0
    
    var targetValue = target.subjectiveValue
    if (target.isTransport && ! target.is(Zerg.Overlord)) {
      targetValue += Protoss.Reaver.subjectiveValue
    }
    
    2.2 * targetValue
  }
}
