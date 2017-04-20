package Information.Battles.Simulation.Construction

import Information.Battles.BattleTypes.BattleGroup
import Information.Battles.TacticsTypes.TacticsOptions

import scala.collection.mutable.ArrayBuffer

class BattleSimulationGroup(battleGroup:BattleGroup, val tactics: TacticsOptions) {
  
  val units: ArrayBuffer[Simulacrum] =
    new ArrayBuffer[Simulacrum] ++
    battleGroup.units
      .filter(_.unitClass.helpsInCombat)
      .map(unit => new Simulacrum(unit))
  
  var lostUnits           : ArrayBuffer[Simulacrum] = ArrayBuffer.empty
  var lostValue           : Int = 0
  var lostValuePerSecond  : Int = 0
}
