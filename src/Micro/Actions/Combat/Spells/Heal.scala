package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Heal extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Medic) && validTargets(unit).nonEmpty && unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = validTargets(unit)
    val target  = ByOption.minBy(targets)(_.pixelDistanceFast(unit))
    
    target.foreach(someTarget => With.commander.attackMove(unit, someTarget.pixelCenter))
  }
  
  private def validTargets(unit: FriendlyUnitInfo): Vector[UnitInfo] = {
    unit.matchups.allies.filter(u => u.unitClass.isOrganic && ! u.beingHealed)
  }
}
