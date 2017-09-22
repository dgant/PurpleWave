package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

object MindControl extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.DarkArchon
  override protected def tech           : Tech      = Protoss.MindControl
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 8
  override protected def thresholdValue : Double    = Terran.Wraith.subjectiveValue
  
  override protected def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding)  return 0.0
    if (target.isFriendly)            return 0.0
    if (target.stasised)              return 0.0
    
    if (target.isTransport && ! target.is(Zerg.Overlord)) return Protoss.Reaver.subjectiveValue / 2.0 + target.subjectiveValue
    
    1.2 * target.subjectiveValue * target.totalHealth / target.unitClass.maxTotalHealth
  }
}
