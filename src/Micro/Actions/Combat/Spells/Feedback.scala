package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetSingle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Feedback extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.DarkArchon)                   &&
    unit.energy >= Protoss.Feedback.energyCost    &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
  
    def valueTarget(target: UnitInfo): Double = {
      val baseValue =
        if ( ! target.isEnemy)
          -1.0
        else if (target.unitClass.isBuilding)
          -1.0
        else if (target.energy >= target.totalHealth)
          2.0 * target.energy
        else
          1.0 * target.energy
      
      val multiplier = if (unit.energy > Protoss.MindControl.energyCost + Protoss.Feedback.energyCost) 3 else 1
      baseValue
    }
    
    val target = TargetSingle.chooseTarget(
      unit,
      32.0 * 15.0,
      200,
      valueTarget)
    
    target.foreach(With.commander.useTechOnUnit(unit, Protoss.MindControl, _))
  }
  
  
}
