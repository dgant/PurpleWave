package Information.Battles.Simulation

import Information.Battles.BattleGroup

import scala.collection.mutable

class BattleSimulationGroup(battleGroup:BattleGroup, val strategy: BattleStrategy) {
  
  def buildSimulacra(battleGroup:BattleGroup):Set[Simulacrum] = {
    battleGroup.units.filter(_.unitClass.helpsInCombat).map(unit => new Simulacrum(unit))
  }
  
  val units: mutable.Set[Simulacrum] =
    new mutable.HashSet[Simulacrum] ++
    battleGroup.units.filter(_.unitClass.helpsInCombat).map(unit => new Simulacrum(unit))
  
  var lostValue:Int = 0
}
