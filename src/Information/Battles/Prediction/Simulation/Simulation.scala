package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.SimulationCheckpoint
import Information.Battles.Types.Battle
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

final class Simulation {
  val resolution: Int = With.configuration.simulationResolution
  var battle: Battle = _
  val realUnits: ArrayBuffer[UnitInfo] = new ArrayBuffer(200)
  val realUnitsOurs: ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val realUnitsEnemy: ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val grid: SimulationGrid = new SimulationGrid

  def reset(newBattle: Battle): Unit = {
    battle = newBattle
    realUnits.clear()
    realUnitsOurs.clear()
    realUnitsEnemy.clear()
    realUnits ++= battle.teams.view.flatMap(_.units).filter(simulatable).toVector.sortBy(_.pixelDistanceSquared(battle.focus))
    realUnitsOurs ++= realUnits.view.filter(_.isOurs)
    realUnitsEnemy ++= realUnits.view.filter(_.isEnemy)
    simulacra.foreach(_.reset(this))
  }

  @inline def step(): Unit = {
    simulacra.foreach(_.act())
    simulacra.foreach(_.update())
   battle.simulationFrames += 1
   battle.simulationComplete ||= battle.simulationFrames >= With.configuration.simulationFrames
   battle.simulationComplete ||= ! simulacraOurs.exists(_.alive)
   battle.simulationComplete ||= ! simulacraEnemy.exists(_.alive)
   battle.simulationComplete ||= ! simulacra.exists(s => s.alive && s.behavior.fighting)
    if (battle.simulationComplete) {
      cleanup()
    } else if (battle.simulationFrames - battle.simulationCheckpoints.lastOption.map(_.framesIn).getOrElse(0) >= With.configuration.simulationResolution) {
      checkpoint()
    }
  }

  @inline private def simulatable(unit: UnitInfo): Boolean = (
    ! unit.unitClass.isSpell
    && ! unit.stasised
    && ! unit.isAny(Protoss.Interceptor, Protoss.Scarab))

  private def cleanup(): Unit = {
   battle.simulationDeaths = simulacra.count(u => u.dead && u.isOurs)
    if (battle.logSimulation) {
     battle.simulationReport ++= simulacra.map(simulacrum => (simulacrum.realUnit, new ReportCard(simulacrum, battle)))
     battle.simulationEvents = simulacra.flatMap(_.events).sortBy(_.frame)
    }
    checkpoint()
  }

  private def checkpoint(): Unit = {
   battle.simulationCheckpoints += new SimulationCheckpoint(this, battle.simulationCheckpoints.lastOption)
  }

  def simulacra       : Seq[Simulacrum] = realUnits.view.map(_.simulacrum)
  def simulacraOurs   : Seq[Simulacrum] = realUnitsOurs.view.map(_.simulacrum)
  def simulacraEnemy  : Seq[Simulacrum] = realUnitsEnemy.view.map(_.simulacrum)
  def simulacraAlliesOf(sim: Simulacrum): Seq[Simulacrum] = (if (sim.isFriendly) simulacraOurs else simulacraEnemy).filterNot(sim==)
  def simulacraEnemiesOf(sim: Simulacrum): Seq[Simulacrum] = if (sim.isFriendly) simulacraEnemy else simulacraOurs
}
