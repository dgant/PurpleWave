package Micro.Actions.Combat.Attacking.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCombatants extends TargetFilter {
  
  // If we're fighting, target units that threaten to fight back
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val teamEngaged = (actor.teammates :+ actor).exists(_.matchups.threatsInRange.nonEmpty)
    
    val output = target.unitClass.helpsInCombat || ! teamEngaged
    
    output
  }
  
}
