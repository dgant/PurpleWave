package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetSingle
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Irradiate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.ScienceVessel)               &&
    unit.energy >= Terran.Irradiate.energyCost  &&
    With.self.hasTech(Terran.Irradiate)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    def valueTarget(target: UnitInfo): Double = {
      val baseValue =
        if ( ! target.isEnemy)
          -1.0
        else if ( ! target.unitClass.isOrganic)
          -1.0
        else if (target.matchups.framesToLive < 24 * 20)
          -1.0
        else if (target.unitClass.gasPrice >= 0)
          1.0
        else
          -1.0
      
      baseValue * unit.subjectiveValue
    }
    
    val target = TargetSingle.chooseTarget(
      unit,
      32.0 * 14.0,
      1.0,
      valueTarget)
    
    target.foreach(With.commander.useTechOnUnit(unit, Terran.Irradiate, _))
  }
  
  
}
