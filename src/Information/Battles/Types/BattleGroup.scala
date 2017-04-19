package Information.Battles.Types

import Information.Battles.Types.Tactics.Tactic
import Mathematics.Pixels.{Pixel, Points}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

class BattleGroup(val units:Vector[UnitInfo]) {
  
  // These should be populated immediately after construction.
  var battle:Battle = _
  var opponent:BattleGroup = _
  
  var strength: Double = 0.0
  var vanguard: Pixel = Points.middle
  
  lazy val mobile:Boolean = units.exists(_.canMoveThisFrame)
  
  lazy val tacticsAvailable:Vector[TacticsOptions] =
    tacticsAvailableMovement.flatten(movement =>
      tacticsAvailableWounded.flatten(wounded =>
        tacticsAvailableFocus.flatten(focus =>
          tacticsAvailableWorkers.map(workers => {
            val output = new TacticsOptions
            output.add(focus)
            output.add(movement)
            output.add(workers)
            output.add(wounded)
            output
          }))))
  
  lazy val tacticsAvailableMovement:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    if (mobile) {
      output += Tactics.Movement.Charge
      output += Tactics.Movement.Flee
    
      if (opponent.mobile) {
        output += Tactics.Movement.Kite
      }
    }
    if (output.isEmpty) {
      output += Tactics.Movement.None
    }
    output.toVector
  }
  
  lazy val tacticsAvailableWounded:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    output += Tactics.Wounded.Fight
    if (mobile) {
      output += Tactics.Wounded.Flee
    }
    output.toVector
  }
  
  lazy val tacticsAvailableFocus:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    output += Tactics.Focus.None
    if (opponent.units.exists(_.flying) && opponent.units.exists( ! _.flying)) {
      output += Tactics.Focus.Air
      output += Tactics.Focus.Ground
    }
    output.toVector
  }
  
  lazy val tacticsAvailableWorkers:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    val workerCount = units.count(_.unitClass.isWorker)
    output += Tactics.Workers.Ignore
    if (workerCount > 0) {
      output += Tactics.Workers.FightAll
      output += Tactics.Workers.Flee
    }
    if (workerCount > 3) {
      output += Tactics.Workers.FightHalf
    }
    output.toVector
  }
}
