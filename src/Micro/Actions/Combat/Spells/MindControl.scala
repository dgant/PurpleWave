package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

object MindControl extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.DarkArchon
  override protected def tech           : Tech      = Protoss.MindControl
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 8
  override protected def thresholdValue : Double    = 0.5 * Protoss.DarkArchon.subjectiveValue
  
  override protected def valueTarget(target: UnitInfo): Double = {
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
