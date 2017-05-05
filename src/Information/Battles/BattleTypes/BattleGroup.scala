package Information.Battles.BattleTypes

import Information.Battles.TacticsTypes.Tactics.Tactic
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Mathematics.Pixels.{Pixel, Points}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

class BattleGroup(val units:Vector[UnitInfo]) {
  
  // These should be populated immediately after construction.
  var battle:Battle = _
  var opponent:BattleGroup = _
  var vanguard: Pixel = Points.middle
  var centroid: Pixel = Points.middle
  
  lazy val visible:Boolean = units.exists(_.visible)
  lazy val mobile:Boolean = units.exists(_.canMoveThisFrame)
  
  lazy val tacticsApparent:TacticsOptions = {
    
    val output = new TacticsOptions
    
    val visibleUnits = units.filter(_.visible)
    
    val meanVelocityX = if (visibleUnits.isEmpty) 0.0 else visibleUnits.map(_.velocityX).sum / visibleUnits.size
    val meanVelocityY = if (visibleUnits.isEmpty) 0.0 else visibleUnits.map(_.velocityY).sum / visibleUnits.size
    val destination = vanguard.add(meanVelocityX.toInt, meanVelocityY.toInt)
    if (destination.pixelDistanceFast(opponent.vanguard) < vanguard.pixelDistanceFast(opponent.vanguard))
      output.add(Tactics.Movement.Charge)
    else if (destination.pixelDistanceFast(opponent.vanguard) > vanguard.pixelDistanceFast(opponent.vanguard))
      output.add(Tactics.Movement.Flee)
    else
      output.add(Tactics.Movement.Regroup)
  
    val woundedUnits = visibleUnits.filter(_.wounded)
    val meanWoundedVelocityX = if (woundedUnits.isEmpty) 0.0 else woundedUnits.map(_.velocityX).sum / woundedUnits.size
    val meanWoundedVelocityY = if (woundedUnits.isEmpty) 0.0 else woundedUnits.map(_.velocityY).sum / woundedUnits.size
    val destinationWounded = vanguard.add(meanWoundedVelocityX.toInt, meanWoundedVelocityY.toInt)
    if (destinationWounded.pixelDistanceFast(opponent.vanguard) > destination.pixelDistanceFast(opponent.vanguard))
      output.add(Tactics.Wounded.Flee)
    else
      output.add(Tactics.Wounded.Fight)
    
    val workers = visibleUnits.filter(_.unitClass.isWorker)
    val fightingWorkers = workers.filter(worker => worker.target.exists(target => target.isEnemyOf(worker)))
    val fleeingWorkers = workers.filter(_.target.isEmpty)
    if (fightingWorkers.size > workers.size * 0.75)
      output.add(Tactics.Workers.FightAll)
    else if (fightingWorkers.size > 0)
      output.add(Tactics.Workers.FightHalf)
    else if (fleeingWorkers.size > workers.size * 0.5)
      output.add(Tactics.Workers.Flee)
    else
      output.add(Tactics.Workers.Ignore)
    
    val attackers = visibleUnits.filter(_.attacking)
    val attackingAir = attackers.filter(_.target.exists(_.flying))
    if (attackingAir.size > attackers.size * 0.8)
      output.add(Tactics.Focus.Air)
    else if (attackingAir.size < attackers.size * 0.2)
      output.add(Tactics.Focus.Ground)
    else
      output.add(Tactics.Focus.None)
  
    output
  }
  
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
      .filterNot(output =>
        (output.has(Tactics.Movement.Flee) && output.has(Tactics.Wounded.Fight))      ||
        (output.has(Tactics.Movement.Flee) && output.has(Tactics.Workers.FightAll))   ||
        (output.has(Tactics.Movement.Flee) && output.has(Tactics.Workers.FightHalf)))
  
  lazy val tacticsAvailableMovement:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    if (mobile) {
      output += Tactics.Movement.Charge
      output += Tactics.Movement.Flee
      output += Tactics.Movement.Regroup
    }
    output.toVector
  }
  
  lazy val tacticsAvailableWounded:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    if (units.exists(unit => unit.wounded && unit.canMoveThisFrame)) {
      output += Tactics.Wounded.Fight
      output += Tactics.Wounded.Flee
    }
    output.toVector
  }
  
  lazy val tacticsAvailableFocus:Vector[Tactic] = {
    val output = new ArrayBuffer[Tactic]
    output += Tactics.Focus.None
    if (
      opponent.units.exists(_.flying) &&
      opponent.units.exists( ! _.flying) &&
      units.exists(_.unitClass.attacksGround) &&
      units.exists(_.unitClass.attacksAir)) {
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
