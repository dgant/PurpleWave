package Debugging

import java.lang.management.ManagementFactory
import java.util.Calendar

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.Performance.ShowPerformanceDetails
import Debugging.Visualizations.Views.Planning.{ShowStrategyEvaluations, ShowStrategyInterest}
import Lifecycle.{JBWAPIClient, Main, With}
import Planning.Predicates.Reactive.{SafeAtHome, SafeToMoveOut}
import Planning.UnitMatchers.MatchHatchlike
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Strategery.Strategies.Strategy
import Utilities.{ByOption, GameTime, Minutes}
import com.sun.management.OperatingSystemMXBean

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Storyteller {

  trait IStory { def update(): Unit}

  class Story[T](label: String, currentValue: () => T, stringify: (T) => String = (x: T) => x.toString) extends IStory {
    var valueLast: T = _
    val valueInitial: T = valueLast
    def update(): Unit = {
      val valueNew = currentValue()
      if (valueLast != valueNew) {
        tell(label + (if (valueLast == valueInitial) " is " else " changed from " + stringify(valueLast) + " to " ) + stringify(valueNew))
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
  private lazy val interestingTechs = Techs.all.filterNot(defaultTechs.contains)

  val stories: Seq[IStory] = Seq(
    new Story[Iterable[PlayerInfo]]     ("Opponents",          () => With.enemies.filter(_.isEnemy),                                                                                  _.map(_.name).mkString(", ")),
    new Story[Iterable[Double]]         ("Rush distances",     () => With.geography.rushDistances,                                                                                    _.map(d => (d / 32).toInt.toString).mkString(", ")),
    new Story                           ("Playbook",           () => With.configuration.playbook),
    new Story                           ("Policy",             () => With.configuration.playbook.policy),
    new Story                           ("Enemy race",         () => With.enemy.raceCurrent),
    new Story[Iterable[Strategy]]       ("Strategy",           () => With.strategy.selectedCurrently,                                                                                 _.map(_.toString).mkString(" ")),
    new Story                           ("Our bases",          () => With.geography.ourBases.size),
    new Story                           ("Enemy bases",        () => With.geography.enemyBases.size),
    new Story[Iterable[Tech]]           ("Our techs",          () => interestingTechs.filter(With.self.hasTech),                                                                      _.map(_.toString).mkString(", ")),
    new Story[Iterable[Tech]]           ("Enemy techs",        () => interestingTechs.filter(t => With.enemies.exists(_.hasTech(t))),                                                 _.map(_.toString).mkString(", ")),
    new Story[Iterable[(Upgrade, Int)]] ("Our upgrades",       () => Upgrades.all.map(u => (u, With.self.getUpgradeLevel(u))).filter(_._2 > 0),                                       _.map(u => f"${u._1} = ${u._2}").mkString(", ")),
    new Story[Iterable[(Upgrade, Int)]] ("Enemy upgrades",     () => Upgrades.all.map(u => (u, ByOption.max(With.enemies.map(_.getUpgradeLevel(u))).getOrElse(0))).filter(_._2 > 0),  _.map(u => f"${u._1} = ${u._2}").mkString(", ")),
    new Story                           ("Our Factories",      () => With.units.countOurs(Terran.Factory)),
    new Story                           ("Our Barracks",       () => With.units.countOurs(Terran.Barracks)),
    new Story                           ("Our Gateways",       () => With.units.countOurs(Protoss.Gateway)),
    new Story                           ("Our Hatcheries",     () => With.units.countOurs(MatchHatchlike)),
    new Story                           ("Enemy Factories",    () => With.units.countEnemy(Terran.Factory)),
    new Story                           ("Enemy Barracks",     () => With.units.countEnemy(Terran.Barracks)),
    new Story                           ("Enemy Gateways",     () => With.units.countEnemy(Protoss.Gateway)),
    new Story                           ("Enemy Hatcheries",   () => With.units.countEnemy(MatchHatchlike)),
    new Story                           ("Safe at home",       () => new SafeAtHome().apply.toString),
    new Story                           ("Safe to move out",   () => new SafeToMoveOut().apply.toString),
    new Story                           ("Should attack",      () => With.blackboard.wantToAttack.get.toString),
    new Story[Iterable[String]]         ("Fingerprints",       () => With.fingerprints.status,                                                                                        _.map(_.toString).mkString(" ")),
    new Story[Iterable[String]]         ("Status",             () => With.blackboard.status.get,                                                                                      _.map(_.toString).mkString(", ")),
    new Story                           ("Performance danger", () => With.performance.danger),
    new Story                           ("Sluggishness",       () => With.reaction.sluggishness)
  )

  var firstLog: Boolean = true
  val performanceThresholds = Vector(
    (Minutes(10), 24),
    (Minutes(15), 24 * 10))
  def onFrame(): Unit = {
    if (firstLog) {
      firstLog = false
      logEnvironment()
      logStrategyEvaluation()
      logStrategyInterest()
    }
    if (performanceThresholds.exists(_._1() == With.frame)) {
      tell("Storyteller moving to more intermittent updates")
    }
    val updateFrequency = ByOption.maxBy(performanceThresholds.filter(_._1() <= With.frame))(_._1()).map(_._2).getOrElse(1)
    if (With.frame % updateFrequency == 0) {
      stories.view.foreach(_.update())
      logIntelligence()
      logOurUnits()
    }
  }

  var enemyUnitsBefore: Set[UnitClass] = Set.empty
  private def logIntelligence(): Unit = {
    val enemyUnitsAfter = With.enemies.flatMap(With.unitsShown.all(_).view).filter(_._2.nonEmpty).map(_._1).toSet
    val enemyUnitsDiff = enemyUnitsAfter -- enemyUnitsBefore
    enemyUnitsDiff.foreach(newType => {
      tell("Discovered novel enemy unit: " + newType)
      With.units.enemy.withFilter(newType).foreach(unit => {
        if (newType.isBuilding) {
          if (unit.complete) {
            tell(unit + " is already complete")
          } else {
            val remainingFrames = unit.completionFrame - With.frame
            tell(unit + " projects to complete in " + remainingFrames + " frames at " + new GameTime(unit.completionFrame) + "")
          }
        } else {
          val arrivalFrame = unit.arrivalFrame()
          val arrivalFramesAhead = arrivalFrame - With.frame
          tell(unit + " projects to arrive in " + arrivalFramesAhead + " frames at " + new GameTime(arrivalFrame))
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
    val gameFastestSeconds = With.frame / 24
    val gameWallClockSeconds = (System.nanoTime() - With.startNanoTime) / 1000000000
    tell("Game duration (fastest):    " + gameFastestSeconds / 60 + "m " + gameFastestSeconds % 60 + "s")
    tell("Game duration (wall clock): " + gameWallClockSeconds / 60 + "m " + gameWallClockSeconds % 60 + "s")
    tell("\n" + Seq(
      Seq(With.configuration.frameLimitMs, With.performance.framesOverShort),
      Seq(1000, With.performance.framesOver1000),
      Seq(10000, With.performance.framesOver10000)).map(line => "Bot frames over " + line.head.toString + "ms: " + line.last.toString).mkString("\n"))
    tell(
      "The bot believes its performance"
      + (if (Main.configuration.getAsync) ", if it were running synchronously, would have been " else " was ")
      + (if (With.performance.disqualified) "BAD" else if (With.performance.danger) "DANGEROUS" else "good"))
    tell(JBWAPIClient.getPerformanceMetrics.toString)
    tell(
      DrawScreen.tableToString(
        DrawScreen.padTable(
          ShowPerformanceDetails.statusTable(
            ShowPerformanceDetails.sortTasks(
              With.performance.tasks)))))
  }

  private def logMemoryUsage(): Unit = {
    tell("JVM Max memory:   " + Runtime.getRuntime.maxMemory()   / 1000000 + " MB")
    tell("JVM Total memory: " + Runtime.getRuntime.totalMemory() / 1000000 + " MB")
    tell("JVM Free memory:  " + Runtime.getRuntime.freeMemory()  / 1000000 + " MB")
  }

  private def logEnvironment(): Unit = {
    tell("Game start time:  " + Calendar.getInstance().getTime.toString)
    tell("OS:               " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"))
    tell("JRE:              " + System.getProperty("java.vendor") + " - " + System.getProperty("java.version"))
    tell("CPUs available:   " + Runtime.getRuntime.availableProcessors())
    tell("System memory:    " + ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getTotalPhysicalMemorySize / 1000000 + " MB")
    logMemoryUsage()

    try {
      val timestamp = Source.fromFile(With.bwapiData.ai + "timestamp.txt").getLines.mkString
      tell("This copy of PurpleWave was packaged for distribution on " + timestamp)
    } catch { case exception: Exception =>
      tell("No deployment timestamp available")
    }

    try {
      val revision = Source.fromFile(With.bwapiData.ai + "revision.txt").getLines.mkString
      tell("This copy of PurpleWave came from Git revision " + revision)
    } catch { case exception: Exception =>
        tell("No deployment Git revision available")
    }
    tell("JBWAPI autocontinue: " + Main.configuration.getAutoContinue)
    tell("JBWAPI debugConnection: " + Main.configuration.getDebugConnection)
    tell("JBWAPI async: " + Main.configuration.getAsync)
    tell("JBWAPI async unsafe: " + Main.configuration.getAsyncUnsafe)
    tell("JBWAPI async frame buffer size: " + Main.configuration.getAsyncFrameBufferCapacity)
    tell("JBWAPI unlimited frame zero: " + Main.configuration.getUnlimitedFrameZero)
    tell("JBWAPI max frame duration: " + Main.configuration.getMaxFrameDurationMs + "ms")
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
    columns.map(_.mkString("\t")).foreach(tell)
  }

  private def logStrategyInterest(): Unit = {
    tell("Strategy interest")
    ShowStrategyInterest.evaluations.map(p => p._1 + " " + p._2).foreach(tell)
  }

  class Tale(val tale: String) { val frame: Int = With.frame }

  val tales: mutable.ArrayBuffer[Tale] = new mutable.ArrayBuffer[Tale]

  private def tell(value: String): Unit = {
    With.logger.debug(value)
    tales += new Tale(value)
  }

  def onEnd(): Unit = {
    logPerformance()
    logMemoryUsage()
  }
}

