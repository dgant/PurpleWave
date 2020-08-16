package Debugging

import Debugging.Visualizations.Views.Planning.{ShowStrategyEvaluations, ShowStrategyInterest}
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.{JBWAPIClient, With}
import Planning.Predicates.Reactive.{SafeAtHome, SafeToMoveOut}
import Planning.UnitMatchers.UnitMatchHatchery
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrades
import Utilities.ByOption
import java.lang.management.ManagementFactory

import com.sun.management.OperatingSystemMXBean

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Storyteller {

  case class Story(label: String, currentValue:     () => String, var valueLast: String = "") {
    def update(): Unit = {
      val valueNew = currentValue()
      if (valueLast != valueNew) {
        With.logger.debug(label + (if (valueLast == "") " is " else " changed from " + valueLast + " to " ) + valueNew)
      }
      valueLast = valueNew
    }
  }

  lazy val defaultTechs = Seq(
    Terran.ScannerSweep,
    Terran.NuclearStrike,
    Terran.DefensiveMatrix,
    Terran.Healing,
    Protoss.ArchonMeld,
    Protoss.Feedback,
    Protoss.DarkArchonMeld,
    Zerg.Parasite,
    Zerg.InfestCommandCenter,
    Zerg.DarkSwarm)
  lazy val interestingTechs = Techs.all.filter(! defaultTechs.contains(_))

  val stories: Seq[Story] = Seq[Story](
    Story("Opponents",          () => With.enemies.filter(_.isEnemy).map(_.name).mkString(", ")),
    Story("Rush distances",     () => With.geography.rushDistances.mkString(", ")),
    Story("Playbook",           () => With.configuration.playbook.toString),
    Story("Policy",             () => With.configuration.playbook.strategySelectionPolicy.toString),
    Story("Enemy race",         () => With.enemy.raceCurrent.toString),
    Story("Strategy",           () => With.strategy.selectedCurrently.map(_.toString).mkString(" ")),
    Story("Our bases",          () => With.geography.ourBases.size.toString),
    Story("Enemy bases",        () => With.geography.enemyBases.size.toString),
    Story("Our techs",          () => interestingTechs.view.filter(With.self.hasTech).mkString(", ")),
    Story("Enemy techs",        () => interestingTechs.view.filter(t => With.enemies.exists(_.hasTech(t))).mkString(", ")),
    Story("Our upgrades",       () => Upgrades.all.view.map(u => (u, With.self.getUpgradeLevel(u))).filter(_._2 > 0).map(u => u._1 + " = " + u._2).mkString(", ")),
    Story("Enemy upgrades",     () => Upgrades.all.view.map(u => (u, ByOption.max(With.enemies.map(_.getUpgradeLevel(u))).getOrElse(0))).filter(_._2 > 0).map(u => u._1 + " = " + u._2).mkString(", ")),
    Story("Our Factories",      () => With.units.countOurs(Terran.Factory).toString),
    Story("Our Barracks",       () => With.units.countOurs(Terran.Barracks).toString),
    Story("Our Gateways",       () => With.units.countOurs(Protoss.Gateway).toString),
    Story("Our Hatcheries",     () => With.units.countOurs(UnitMatchHatchery).toString),
    Story("Enemy Factories",    () => With.units.countEnemy(Terran.Factory).toString),
    Story("Enemy Barracks",     () => With.units.countEnemy(Terran.Barracks).toString),
    Story("Enemy Gateways",     () => With.units.countEnemy(Protoss.Gateway).toString),
    Story("Enemy Hatcheries",   () => With.units.countEnemy(UnitMatchHatchery).toString),
    Story("Safe at home",       () => new SafeAtHome().isComplete.toString),
    Story("Safe to move out",   () => new SafeToMoveOut().isComplete.toString),
    Story("Should attack",      () => With.blackboard.wantToAttack.get.toString),
    Story("Fingerprints",       () => With.fingerprints.status.mkString(" ")),
    Story("Status",             () => With.blackboard.status.get.mkString(", ")),
    Story("Performance danger", () => With.performance.danger.toString)
  )
  var firstLog: Boolean = true
  def onFrame(): Unit = {
    stories.foreach(_.update())
    logIntelligence()
    logOurUnits()
    if (firstLog) {
      firstLog = false
      logEnvironment()
      logStrategyEvaluation()
      logStrategyInterest()
    }
  }

  var enemyUnitsBefore: Set[UnitClass] = Set.empty
  private def logIntelligence(): Unit = {
    val enemyUnitsAfter = With.enemies.flatMap(With.unitsShown.all(_).view).filter(_._2.nonEmpty).map(_._1).toSet
    val enemyUnitsDiff = enemyUnitsAfter -- enemyUnitsBefore
    enemyUnitsDiff.foreach(newType => {
      With.logger.debug("Discovered novel enemy unit: " + newType)
      With.units.enemy.withFilter(_.is(newType)).foreach(unit => {
        if (newType.isBuilding) {
          if (unit.complete) {
            With.logger.debug(unit + " is already complete")
          } else {
            val remainingFrames = unit.completionFrame - With.frame
            With.logger.debug(unit + " projects to complete in " + remainingFrames + " frames at " + new GameTime(unit.completionFrame) + "")
          }
        } else {
          val arrivalFrame = unit.arrivalFrame()
          val arrivalFramesAhead = arrivalFrame - With.frame
          With.logger.debug(unit + " projects to arrive in " + arrivalFramesAhead + " frames at " + new GameTime(arrivalFrame))
        }
      })
    })
    enemyUnitsBefore = enemyUnitsAfter
  }

  private var ourUnitsBefore: mutable.Set[UnitClass] = mutable.HashSet.empty
  private def logOurUnits(): Unit = {
    val ourUnitsAfter = With.units.ours.map(_.unitClass).toSet
    val ourUnitsNew = ourUnitsAfter -- enemyUnitsBefore
    ourUnitsNew.foreach(unit => "New unit")
  }

  private def logPerformance() {
    With.logger.debug(Seq(
      Seq(With.performance.frameLimitShort, With.performance.framesOverShort),
      Seq(1000, With.performance.framesOver1000),
      Seq(10000, With.performance.framesOver10000)).map(line => line.head.toString + "ms: " + line.last.toString).mkString("\n"))
    With.logger.debug("Our performance was " + (if (With.performance.disqualified) "BAD" else if (With.performance.danger) "DANGEROUS" else "good"))
    With.logger.debug(JBWAPIClient.getPerformanceMetrics.toString)
  }

  private def logEnvironment(): Unit = {
    With.logger.debug("OS:               " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"))
    With.logger.debug("JRE:              " + System.getProperty("java.vendor") + " - " + System.getProperty("java.version"))
    With.logger.debug("CPUs available:   " + Runtime.getRuntime.availableProcessors())
    With.logger.debug("System memory:    " + ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getTotalPhysicalMemorySize / 1000000 + " MB")
    With.logger.debug("JVM Max memory:   " + Runtime.getRuntime.maxMemory()   / 1000000 + " MB")
    With.logger.debug("JVM Total memory: " + Runtime.getRuntime.totalMemory() / 1000000 + " MB")
    With.logger.debug("JVM Free memory:  " + Runtime.getRuntime.freeMemory()  / 1000000 + " MB")
  }

  private def logStrategyEvaluation(): Unit = {
    val columns = ShowStrategyEvaluations.columns
    if (columns.isEmpty) return
      var rows = new ArrayBuffer[String]()
      (0 until columns.map(_.length).max).foreach(rowIndex => {
        var row: String = ""
        columns.foreach(column => {
          if (rowIndex < column.length) row += column(rowIndex)
        })
        rows += row
     })
    columns.map(_.mkString("\t")).foreach(With.logger.debug)
  }

  private def logStrategyInterest(): Unit = {
    With.logger.debug("Strategy interest")
    ShowStrategyInterest.evaluations.map(p => p._1 + " " + p._2).foreach(With.logger.debug)
  }

  def onEnd(): Unit = {
    logPerformance()
  }
}
