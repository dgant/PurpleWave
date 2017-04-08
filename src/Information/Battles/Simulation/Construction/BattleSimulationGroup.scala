package Information.Battles.Simulation.Construction

import Information.Battles.BattleGroup
import Information.Battles.Simulation.Tactics.Tactics

import scala.collection.mutable.ListBuffer

class BattleSimulationGroup(battleGroup:BattleGroup, val tactics: Tactics) {
  
  val units: ListBuffer[Simulacrum] =
    new ListBuffer[Simulacrum] ++
    battleGroup.units.filter(_.unitClass.helpsInCombat).map(unit => new Simulacrum(unit))
  
  var lostValue:Int = 0
  var lostValuePerSecond:Int = 0
}
