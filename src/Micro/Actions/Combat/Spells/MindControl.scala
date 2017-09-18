package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetSingle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object MindControl extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.DarkArchon)                   &&
    unit.energy >= Protoss.MindControl.energyCost &&
    With.self.hasTech(Protoss.MindControl)        &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
  
    def valueTarget(target: UnitInfo): Double = {
      val output =
        if ( ! target.isEnemy)
          -1.0
        else if (target.unitClass.isBuilding)
          -1.0
        else if (target.unitClass.gasValue >= 200)
          target.subjectiveValue
        else
          -1.0
      
      output
    }
    
    val target = TargetSingle.chooseTarget(
      unit,
      32.0 * 14.0,
      0.0,
      valueTarget)
    
    target.foreach(With.commander.useTechOnUnit(unit, Protoss.MindControl, _))
  }
  
  
}
