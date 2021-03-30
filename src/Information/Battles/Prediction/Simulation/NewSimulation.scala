package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.{PredictionLocal, SimulationCheckpoint}
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

class NewSimulation {
  val resolution = 6
  var prediction: PredictionLocal = _
  var logEvents: Boolean = _
  var complete: Boolean = false
  val checkpoints: ArrayBuffer[SimulationCheckpoint] = ArrayBuffer.empty
  val realUnits: ArrayBuffer[UnitInfo] = new ArrayBuffer(200)
  val realUnitsOurs: ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val realUnitsEnemy: ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val grid: SimulationGrid = new SimulationGrid
  def setup(newPrediction: PredictionLocal): Unit = {
    prediction = newPrediction
    // TODO
    // prediction.simulation = this
    prediction.frames = 0
    complete = false
    checkpoints.clear()
    realUnits.clear()
    realUnitsOurs.clear()
    realUnitsEnemy.clear()
    realUnits ++= prediction.battle.units.filter(simulatable)
    realUnits.sortBy(_.pixelDistanceSquared(prediction.battle.focus))
    realUnitsOurs ++= realUnits.view.filter(_.isOurs)
    realUnitsEnemy ++= realUnits.view.filter(_.isEnemy)
    simulacra.foreach(_.reset(this))
  }

  def step(): Unit = {
    simulacra.foreach(_.act())
    simulacra.foreach(_.update())
    prediction.frames += 1
    complete ||= prediction.frames >= 24 * 10
    complete ||= ! simulacraOurs.exists(_.alive)
    complete ||= ! simulacraEnemy.exists(_.alive)
    complete ||= ! simulacra.exists(s => s.alive && s.behavior.fighting)
    if (complete) {
      cleanup()
    } else if (prediction.frames - prediction.localBattleMetrics.lastOption.map(_.framesIn).getOrElse(0) >= With.configuration.simulationEstimationPeriod) {
      checkpoint()
    }
  }

  private def simulatable(unit: UnitInfo): Boolean = (
    ! unit.unitClass.isSpell
    && ! unit.stasised
    && ! unit.isAny(Protoss.Interceptor, Protoss.Scarab))

  private def cleanup(): Unit = {
    prediction.deathsUs = simulacra.count(u => u.dead && u.isOurs)
    if (With.configuration.debugging) {
      // TODO
      // prediction.debugReport ++= everyone.map(simulacrum => (simulacrum.realUnit, simulacrum.reportCard))
      // prediction.events = everyone.flatMap(_.events).sortBy(_.frame)
    }
    checkpoint()
  }
  private def checkpoint(): Unit = {
    // TODO:
    // checkpoints += new SimulationCheckpoint(this, prediction.localBattleMetrics.lastOption)
  }

  def simulacra       : Seq[NewSimulacrum] = realUnits.view.map(_.simulacrum)
  def simulacraOurs   : Seq[NewSimulacrum] = realUnitsOurs.view.map(_.simulacrum)
  def simulacraEnemy  : Seq[NewSimulacrum] = realUnitsEnemy.view.map(_.simulacrum)
}
