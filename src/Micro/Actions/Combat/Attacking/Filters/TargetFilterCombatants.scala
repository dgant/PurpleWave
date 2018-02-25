package Micro.Actions.Combat.Attacking.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCombatants extends TargetFilter {
  
  // If we're fighting, target units that threaten to fight back
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val teamEngaged = (actor.teammates :+ actor).exists(_.matchups.framesOfSafetyDiffused <= 0.0)
    
    val output = target.unitClass.helpsInCombat || target.is(Zerg.LurkerEgg) || ! teamEngaged
    
    output
  }
  
}
