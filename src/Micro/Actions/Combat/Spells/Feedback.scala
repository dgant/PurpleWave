package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

object Feedback extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.DarkArchon
  override protected def tech           : Tech      = Protoss.Feedback
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 10
  override protected def thresholdValue : Double    = Protoss.DarkArchon.subjectiveValue / 3.0
  
  override protected def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding)  return -1.0
    if ( ! target.isEnemy)            return -1.0
    
    val fatalityBonus = if (target.energy >= target.totalHealth) 2.0 else 1.0
    val damageValue   = target.unitClass.subjectiveValue * Math.min(target.energy, target.totalHealth) / target.unitClass.maxTotalHealth
    val output        = fatalityBonus * damageValue
    output
  }
}
