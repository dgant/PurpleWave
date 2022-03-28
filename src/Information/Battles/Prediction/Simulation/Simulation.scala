package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.{PredictionLocal, SimulationCheckpoint}
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

final class Simulation {
  val resolution: Int = With.configuration.simulationResolution
  var prediction: PredictionLocal = _
  val realUnits: ArrayBuffer[UnitInfo] = new ArrayBuffer(200)
  val realUnitsOurs: ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val realUnitsEnemy: ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val grid: SimulationGrid = new SimulationGrid
  def reset(newPrediction: PredictionLocal): Unit = {
    prediction = newPrediction
    realUnits.clear()
    realUnitsOurs.clear()
    realUnitsEnemy.clear()
    realUnits ++= prediction.battle.units.filter(simulatable).sortBy(_.pixelDistanceSquared(prediction.battle.focus))
    realUnitsOurs ++= realUnits.view.filter(_.isOurs)
    realUnitsEnemy ++= realUnits.view.filter(_.isEnemy)
    simulacra.foreach(_.reset(this))
  }

  @inline def step(): Unit = {
    simulacra.foreach(_.act())
    simulacra.foreach(_.update())
    prediction.simulationFrames += 1
    prediction.predictionComplete ||= prediction.simulationFrames >= With.configuration.simulationFrames
    prediction.predictionComplete ||= ! simulacraOurs.exists(_.alive)
    prediction.predictionComplete ||= ! simulacraEnemy.exists(_.alive)
    prediction.predictionComplete ||= ! simulacra.exists(s => s.alive && s.behavior.fighting)
    if (prediction.predictionComplete) {
      cleanup()
    } else if (prediction.simulationFrames - prediction.simulationCheckpoints.lastOption.map(_.framesIn).getOrElse(0) >= With.configuration.simulationResolution) {
      checkpoint()
    }
  }

  @inline private def simulatable(unit: UnitInfo): Boolean = (
    ! unit.unitClass.isSpell
    && ! unit.stasised
    && ! unit.isAny(Protoss.Interceptor, Protoss.Scarab))

  private def cleanup(): Unit = {
    prediction.simulationDeaths = simulacra.count(u => u.dead && u.isOurs)
    if (prediction.logSimulation) {
      prediction.simulationReport ++= simulacra.map(simulacrum => (simulacrum.realUnit, new ReportCard(simulacrum, prediction)))
      prediction.simulationEvents = simulacra.flatMap(_.events).sortBy(_.frame)
    }
    checkpoint()
  }
  private def checkpoint(): Unit = {
    prediction.simulationCheckpoints += new SimulationCheckpoint(this, prediction.simulationCheckpoints.lastOption)
  }

  def simulacra       : Seq[Simulacrum] = realUnits.view.map(_.simulacrum)
  def simulacraOurs   : Seq[Simulacrum] = realUnitsOurs.view.map(_.simulacrum)
  def simulacraEnemy  : Seq[Simulacrum] = realUnitsEnemy.view.map(_.simulacrum)
  def simulacraAlliesOf(sim: Simulacrum): Seq[Simulacrum] = (if (sim.isFriendly) simulacraOurs else simulacraEnemy).filterNot(sim==)
  def simulacraEnemiesOf(sim: Simulacrum): Seq[Simulacrum] = if (sim.isFriendly) simulacraEnemy else simulacraOurs
}
