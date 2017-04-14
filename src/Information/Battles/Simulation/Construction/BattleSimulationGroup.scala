package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.Tactics.Tactics
import Information.Battles.Types.BattleGroup

import scala.collection.mutable.ListBuffer

class BattleSimulationGroup(battleGroup:BattleGroup, val tactics: Tactics) {
  
  val units: ListBuffer[Simulacrum] =
    new ListBuffer[Simulacrum] ++
    battleGroup.units.filter(_.unitClass.helpsInCombat).map(unit => new Simulacrum(unit))
  
  var lostUnits           : ListBuffer[Simulacrum] = ListBuffer.empty
  var lostValue           : Int = 0
  var lostValuePerSecond  : Int = 0
}
