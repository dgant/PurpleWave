package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCombatants extends TargetFilter {
  
  // If we're fighting, target units that threaten to fight back
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    
    if (actor.battle.forall(_.estimationSimulationAttack.deathsUs == 0)) return true
    
    lazy val teamEngaged = (actor.teammates + actor).exists(_.matchups.framesOfSafety <= 0.0)
    
    val output = (
      target.unitClass.dealsDamage
        || target.is(Zerg.LurkerEgg)
        || target.is(Terran.Dropship)
        || target.is(Protoss.Shuttle)
        || ! teamEngaged
      )
    
    output
  }
  
}
