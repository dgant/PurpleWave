package Information.Battles.Prediction.Simulation

import Debugging.CombatVisIO
import Information.Battles.Prediction.SimulationCheckpoint
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.{Pixel, Points}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.SpinWait

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
  var shouldReset     : Boolean               = true
  // Frames log for visualizer (only when debugging)
  private val framesLog: ArrayBuffer[String]  = new ArrayBuffer(4096)
  private var simulationStartGameFrame: Int = 0

  @inline def step(): Unit = {
    if (shouldReset) {
      try {
        With.units.mutex.lock()
        realUnits       .clear()
        realUnitsOurs   .clear()
        realUnitsEnemy  .clear()
        realUnits       ++= battle.teams.view.flatMap(_.units).filter(simulatable).toVector.sortBy(_.pixelDistanceSquared(battle.focus))
        realUnitsOurs   ++= realUnits.view.filter(_.isOurs)
        realUnitsEnemy  ++= realUnits.view.filter(_.isEnemy)
        enemyVanguard     = battle.enemy.vanguardKey()
        engaged           = false
        With.simulation.grid.reset()
        simulacra.foreach(_.reset(this))
        if (battle.logSimulation) {
          framesLog.clear()
          simulationStartGameFrame = With.frame
        }
        shouldReset = false
      } finally {
        With.units.mutex.unlock()
      }
    }
    simulacra.foreach(_.act())
    simulacra.foreach(_.update())
    battle.simulationFrames += 1
    if (battle.logSimulation) {
      val sb = new StringBuilder(1024)
      sb.append(battle.simulationFrames).append('|')
      // Log all units in a stable order (realUnits order is stable during a sim)
      var i = 0
      val sims = simulacra
      while (i < sims.length) {
        val s = sims(i)
        val w = s.unitClass.dimensionRightInclusive + s.unitClass.dimensionLeft + 1
        val h = s.unitClass.dimensionDownInclusive + s.unitClass.dimensionUp + 1
        val hpNow = Math.max(0, s.hitPoints)
        val shNow = Math.max(0, s.shieldPoints)
        val hpMax = Math.max(hpNow, s.hitPointsInitial)
        val shMax = Math.max(shNow, s.shieldPointsInitial)
        val tgtId = s.target.map(_.realUnit.id).getOrElse(-1)
        sb.append(s.realUnit.id).append(',')
          .append(s.isFriendly).append(',')
          .append(s.pixel.x).append(',')
          .append(s.pixel.y).append(',')
          .append(s.alive).append(',')
          .append(w).append(',')
          .append(h).append(',')
          .append(hpNow).append(',')
          .append(shNow).append(',')
          .append(hpMax).append(',')
          .append(shMax).append(',')
          .append(tgtId).append(',')
          .append(s.flying).append(',')
          .append(s.cooldownLeft).append(',')
          .append(s.unitClass.base.toString).append(';')
        i += 1
      }
      // Append per-frame events (attacks and deaths)
      import Information.Battles.Prediction.Simulation.{SimulationEventAttack, SimulationEventDeath}
      sb.append('|')
      var j = 0
      while (j < sims.length) {
        val s = sims(j)
        var k = 0
        val evs = s.events
        while (k < evs.length) {
          val e = evs(k)
          if (e.frame == battle.simulationFrames - 1) {
            e match {
              case a: SimulationEventAttack =>
                sb.append("a:").append(a.shooter.realUnit.id).append('>').append(a.victim.realUnit.id).append(';')
              case d: SimulationEventDeath =>
                sb.append("d:").append(d.simulacrum.realUnit.id).append(';')
              case _ =>
            }
          }
          k += 1
        }
        j += 1
      }
      framesLog += sb.toString()
    }
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
      // Diagnostics
      try {
        val rFramesLimit = battle.simulationFrames >= With.configuration.simulationFrames
        val rNoOurs      = ! simulacraOurs.exists(_.alive)
        val rNoEnemy     = ! simulacraEnemy.exists(_.alive)
        val rNoFighting  = ! simulacra.exists(s => s.alive && s.behavior.fighting)
      } catch { case _: Throwable => }
      // Write simulation frames for visualizer (async to avoid blocking main thread)
      try { CombatVisIO.writeSimulationLogAsync(battle, framesLog, simulationStartGameFrame) } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
      // Disabled because we don't want -- and never wanted -- separate files for each simulation.
      //try { CombatVisIO.writeCompressedSimDumpIfNeededAsync(battle, framesLog, simulationStartGameFrame) } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
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
      thread = new Thread(() =>
        while ( ! terminateThread) {
            if (simulatingAsynchronously) {
              while ( ! battle.simulationComplete) {
                try {
                  step()
                } catch { case exception: Exception =>
                  simulatingAsynchronously = false
                  With.logger.onException(exception)
                  Thread.sleep(1) // Hopefully whatever caused the problem is gone 1ms later
                }
              }
              simulatingAsynchronously = false
            }
            SpinWait()
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
