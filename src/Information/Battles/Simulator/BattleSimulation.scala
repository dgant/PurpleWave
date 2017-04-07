package Information.Battles.Simulator

import Information.Battles.{Battle, BattleGroup}

import scala.collection.mutable

class BattleSimulation(battle:Battle) {
  
  def buildSimulacra(battleGroup:BattleGroup):Set[Simulacrum] = {
    battleGroup.units.filter(_.unitClass.helpsInCombat).map(unit => new Simulacrum(unit))
  }
  
  val ourUnits    : mutable.Set[Simulacrum] = new mutable.HashSet[Simulacrum] ++ buildSimulacra(battle.us)
  val enemyUnits  : mutable.Set[Simulacrum] = new mutable.HashSet[Simulacrum] ++ buildSimulacra(battle.enemy)
  
  var ourLostValue    : Int = 0
  var enemyLostValue  : Int = 0
}
