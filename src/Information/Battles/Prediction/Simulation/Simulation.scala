package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.SimulationCheckpoint
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.{Pixel, Points}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

final class Simulation {
  val resolution      : Int                   = With.configuration.simulationResolution
  var battle          : Battle                = _
  val realUnits       : ArrayBuffer[UnitInfo] = new ArrayBuffer(200)
  val realUnitsOurs   : ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val realUnitsEnemy  : ArrayBuffer[UnitInfo] = new ArrayBuffer(100)
  val grid            : SimulationGrid        = new SimulationGrid
  var enemyVanguard   : Pixel                 = Points.middle
  var engaged         : Boolean               = false

  def reset(newBattle: Battle): Unit = {
    battle            = newBattle
    realUnits       .clear()
    realUnitsOurs   .clear()
    realUnitsEnemy  .clear()
    realUnits       ++= battle.teams.view.flatMap(_.units).filter(simulatable).toVector.sortBy(_.pixelDistanceSquared(battle.focus))
    realUnitsOurs   ++= realUnits.view.filter(_.isOurs)
    realUnitsEnemy  ++= realUnits.view.filter(_.isEnemy)
    enemyVanguard     = battle.enemy.vanguardKey()
    engaged           = battle.units.exists(_.matchups.engagedUpon)
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
    &&  ! unit.stasised
    &&  ! unit.invincible
    && unit.isNone(Protoss.Interceptor, Protoss.Scarab, Zerg.Egg, Zerg.Larva))

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

  def simulacra       : Seq[Simulacrum] = realUnits       .view.map(_.simulacrum)
  def simulacraOurs   : Seq[Simulacrum] = realUnitsOurs   .view.map(_.simulacrum)
  def simulacraEnemy  : Seq[Simulacrum] = realUnitsEnemy  .view.map(_.simulacrum)
  def simulacraAlliesOf(sim: Simulacrum): Seq[Simulacrum] = (if (sim.isFriendly) simulacraOurs else simulacraEnemy).filterNot(sim==)
  def simulacraEnemiesOf(sim: Simulacrum): Seq[Simulacrum] = if (sim.isFriendly) simulacraEnemy else simulacraOurs

  ////////////////
  // Asynchrony //
  ////////////////

  private var thread: Thread = _
  @volatile private var terminateThread: Boolean = _
  @volatile var simulatingAsynchronously = false
  def runAsynchronously(): Unit = {
    if (thread == null) {
      thread = new Thread(() => {
        while ( ! terminateThread) {
          if (simulatingAsynchronously) {
            while ( ! battle.simulationComplete) {
              step()
            }
            simulatingAsynchronously = false
          }
          Thread.onSpinWait()
        }
      })
      thread.setName("CombatSimulation")
      thread.setPriority(Math.max(Thread.currentThread().getPriority - 1, Thread.MIN_PRIORITY))
      thread.start()
    }
    simulatingAsynchronously = true
  }
  def onEnd(): Unit = {
    terminateThread = true
  }
}
