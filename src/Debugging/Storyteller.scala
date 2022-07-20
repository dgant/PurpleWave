package Debugging

import java.lang.management.{ManagementFactory, MemoryType}
import java.text.DecimalFormat
import java.util.Calendar
import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.Performance.ShowPerformance
import Lifecycle.{PurpleBWClient, Main, With}
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Strategery.Strategies.Strategy
import Utilities.Time.{Frames, Minutes}
import com.sun.management.OperatingSystemMXBean

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.Source

class Storyteller {

  trait IStory { def update(): Unit }

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

  private lazy val defaultTechs = Seq(
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

  lazy val rushDistance = f"Rush distances on ${With.mapCleanName}"
  val stories: Seq[IStory] = Seq(
    new Story[Iterable[PlayerInfo]]     ("Opponents",           () => With.enemies.filter(_.isEnemy),                                                                              _.map(_.name).mkString(", ")),
    new Story[Iterable[Int]]            (rushDistance,          () => With.geography.rushDistances,                                                                                _.map(_.toString).mkString(", ")),
    new Story                           ("Mean rush distance",  () => With.strategy.rushDistanceMean),
    new Story                           ("Playbook",            () => With.configuration.playbook),
    new Story                           ("Policy",              () => With.configuration.playbook.policy),
    new Story                           ("Enemy race",          () => With.enemy.raceCurrent),
    new Story[mutable.Set[Strategy]]    ("Strategy",            () => With.strategy.selected,                                                                                      _.map(_.toString).mkString(" ")),
    new Story                           ("Our bases",           () => With.geography.ourBases.size),
    new Story                           ("Enemy bases",         () => With.geography.enemyBases.size),
    new Story[Iterable[Tech]]           ("Our techs",           () => interestingTechs.filter(With.self.hasTech),                                                                  _.map(_.toString).mkString(", ")),
    new Story[Iterable[Tech]]           ("Enemy techs",         () => interestingTechs.filter(t => With.enemies.exists(_.hasTech(t))),                                             _.map(_.toString).mkString(", ")),
    new Story[Iterable[(Upgrade, Int)]] ("Our upgrades",        () => Upgrades.all.map(u => (u, With.self.getUpgradeLevel(u))).filter(_._2 > 0),                                   _.map(u => f"${u._1} = ${u._2}").mkString(", ")),
    new Story[Iterable[(Upgrade, Int)]] ("Enemy upgrades",      () => Upgrades.all.map(u => (u, Maff.max(With.enemies.map(_.getUpgradeLevel(u))).getOrElse(0))).filter(_._2 > 0),  _.map(u => f"${u._1} = ${u._2}").mkString(", ")),
    new Story                           ("Our Factories",       () => With.units.countOurs(Terran.Factory)),
    new Story                           ("Our Barracks",        () => With.units.countOurs(Terran.Barracks)),
    new Story                           ("Our Gateways",        () => With.units.countOurs(Protoss.Gateway)),
    new Story                           ("Our Hatcheries",      () => With.units.countOurs(_.unitClass.isHatchlike)),
    new Story                           ("Enemy Factories",     () => With.units.countEnemy(Terran.Factory)),
    new Story                           ("Enemy Barracks",      () => With.units.countEnemy(Terran.Barracks)),
    new Story                           ("Enemy Gateways",      () => With.units.countEnemy(Protoss.Gateway)),
    new Story                           ("Enemy Hatcheries",    () => With.units.countEnemy(_.unitClass.isHatchlike)),
    new Story                           ("Safe at home",        () => MacroFacts.safeAtHome),
    new Story                           ("Safe to move out",    () => MacroFacts.safeToMoveOut),
    new Story                           ("Should attack",       () => With.blackboard.wantToAttack.get),
    new Story[Iterable[String]]         ("Fingerprints",        () => With.fingerprints.status,                                                                                    _.mkString(" ")),
    new Story[Iterable[String]]         ("Status",              () => With.blackboard.status.get,                                                                                  _.mkString(", ")),
    new Story                           ("Performance danger",  () => With.performance.danger),
    new Story                           ("Sluggishness",        () => With.reaction.sluggishness),
    new Story                           ("Gas worker floor",    () => With.blackboard.gasWorkerFloor()),
    new Story                           ("Gas worker ceiling",  () => With.blackboard.gasWorkerCeiling()),
    new Story                           ("Gas worker ratio",    () => With.blackboard.gasWorkerRatio()),
    new Story                           ("Gas limit floor",     () => With.blackboard.gasLimitFloor()),
    new Story                           ("Gas limit ceiling",   () => With.blackboard.gasLimitCeiling()),
  )

  private var firstLog: Boolean = true
  private val performanceThresholds = Vector(
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
    val updateFrequency = Maff.maxBy(performanceThresholds.filter(_._1() <= With.frame))(_._1()).map(_._2).getOrElse(1)
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
            tell(unit + " projects to complete in " + remainingFrames + " frames at " + Frames(unit.completionFrame) + "")
          }
        } else {
          val arrivalFrame = unit.arrivalFrame()
          val arrivalFramesAhead = arrivalFrame - With.frame
          tell(unit + " projects to arrive in " + arrivalFramesAhead + " frames at " + Frames(arrivalFrame))
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

  private def logPerformance(): Unit = {
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
      + (if (Main.jbwapiConfiguration.getAsync) ", if it were running synchronously, would have been " else " was ")
      + (if (With.performance.disqualified) "BAD" else if (With.performance.danger) "DANGEROUS" else "good"))
    tell(PurpleBWClient.getPerformanceMetrics.toString)
    tell(
      DrawScreen.tableToString(
        DrawScreen.padTable(
          ShowPerformance.statusTable(
            ShowPerformance.sortTasks(
              With.performance.tasks)))))

    val formatterMs = new DecimalFormat("#.###")
    tell(
      "Actions:\n"
      + DrawScreen.tableToString(
        DrawScreen.padTable(
          Vector(Vector("Action", "Invocations", "Mean (ms)", "Total (secs)"))
          ++ With.agents.actionPerformance.toVector.sortBy(- _._2.meanMs).map(p => Vector(
            p._1.name,
            p._2.invocations.toString,
            formatterMs.format(p._2.meanMs),
            formatterMs.format(p._2.totalMs.toInt / 1000))))))
    tell(
      "Pathfinding:\n"
      + DrawScreen.tableToString(
        DrawScreen.padTable(
          Vector(
            Vector("A* pathfinds:", With.paths.aStarPathfinds.toString),
            Vector("Exploration maxed:", With.paths.aStarExplorationMaxed.toString),
            Vector("Over 1ms:", With.paths.aStarOver1ms.toString),
            Vector("Max ms:", (With.paths.aStarNanosMax / 1e6).toString),
            Vector("Mean ms:", formatterMs.format(With.paths.aStarNanosTotal / 1e6d / With.paths.aStarPathfinds)),
            Vector("Path length, max:", With.paths.aStarPathLengthMax.toString),
            Vector("Path length, mean:", formatterMs.format(With.paths.aStarPathLengthTotal.toDouble / With.paths.aStarPathfinds)),
            Vector("Tiles explored, max:", With.paths.aStarTilesExploredMax.toString),
            Vector("Tiles explored, mean:", (With.paths.aStarTilesExploredTotal / With.paths.aStarPathfinds).toString)))))
  }

  private def fmtMem(value: Long): String = f"${value / 1000000} MB"
  private def logMemoryUsage(): Unit = {
    val totalHeap = ManagementFactory.getMemoryPoolMXBeans.asScala.filter(_.getType == MemoryType.HEAP).map(_.getUsage.getUsed).sum
    tell(f"JVM Max memory:   ${fmtMem(Runtime.getRuntime.maxMemory)}")
    tell(f"JVM Total memory: ${fmtMem(Runtime.getRuntime.totalMemory)}")
    tell(f"JVM Free memory:  ${fmtMem(Runtime.getRuntime.freeMemory)}")
    tell(f"JVM Used memory:  ${fmtMem(Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory)}") // https://stackoverflow.com/a/18375641
    tell(f"JVM Used, heap:   ${fmtMem(totalHeap)}")
    ManagementFactory.getMemoryPoolMXBeans.asScala.sortBy(_.getUsage.getUsed).foreach(bean => {
      val u = bean.getUsage
      val p = bean.getPeakUsage
      tell(f"${bean.getType} ${bean.getName}, current: ".padTo(49, ' ') + f"${fmtMem(u.getUsed)} used | ${fmtMem(u.getInit)} init | ${fmtMem(u.getMax)} max | ${fmtMem(u.getCommitted)} committed")
      tell(f"${bean.getType} ${bean.getName}, peak:    ".padTo(49, ' ') + f"${fmtMem(p.getUsed)} used | ${fmtMem(p.getInit)} init | ${fmtMem(p.getMax)} max | ${fmtMem(p.getCommitted)} committed")
    })
  }

  private def logEnvironment(): Unit = {
    tell("Game start time:  " + Calendar.getInstance().getTime.toString)
    tell("OS:               " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"))
    tell("JRE:              " + System.getProperty("java.vendor") + " - " + System.getProperty("java.version"))
    tell("CPUs available:   " + Runtime.getRuntime.availableProcessors())
    tell("System memory:    " + ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean].getTotalPhysicalMemorySize / 1000000 + " MB")
    tell("JVM arguments     " + Main.jvmRuntimeArguments.mkString(" "))
    tell("Live debugging:   " + Main.liveDebugging)
    tell("Buffer capacity:  " + Main.framesBufferable)
    tell("Using buffer?     " + Main.useFrameBuffer)
    logMemoryUsage()

    try {
      tell(f"This copy of PurpleWave was packaged for distribution on ${Source.fromFile(f"${With.bwapiData.ai}timestamp.txt").getLines.mkString}")
    } catch { case exception: Exception => tell("No deployment timestamp available") }
    try {
      tell(f"This copy of PurpleWave came from Git revision ${Source.fromFile(f"${With.bwapiData.ai}revision.txt").getLines.mkString}")
    } catch { case exception: Exception => tell("No deployment Git revision available") }
    tell("JBWAPI autocontinue: " + Main.jbwapiConfiguration.getAutoContinue)
    tell("JBWAPI debugConnection: " + Main.jbwapiConfiguration.getDebugConnection)
    tell("JBWAPI async: " + Main.jbwapiConfiguration.getAsync)
    tell("JBWAPI async unsafe: " + Main.jbwapiConfiguration.getAsyncUnsafe)
    tell("JBWAPI async frame buffer size: " + Main.jbwapiConfiguration.getAsyncFrameBufferCapacity)
    tell("JBWAPI unlimited frame zero: " + Main.jbwapiConfiguration.getUnlimitedFrameZero)
    tell("JBWAPI max frame duration: " + Main.jbwapiConfiguration.getMaxFrameDurationMs + "ms")
    tell(f"Map file: ${With.mapFileName}")
    tell(f"Map hash: ${With.game.mapHash}")
    tell(f"Map id: ${With.mapCleanName}")
    tell(f"Start: ${With.mapClock} o'clock")
  }

  private def logStrategyEvaluation(): Unit = {
    val evaluations = With.strategy.evaluations.values.filter(e => e.strategy.legality.isLegal || e.gamesUs.nonEmpty).toVector.sortBy( - _.probabilityWin)
    val columns = Vector(
      Vector("Strategy") ++ evaluations.map(_.strategy.toString),
      Vector("#Games")   ++ evaluations.map(e => formatGames(e.gamesUs.size)),
      Vector("#Wtd")     ++ evaluations.map(e => formatGames(e.gamesUs.map(_.weight).sum)),
      Vector("#WtdWins") ++ evaluations.map(e => formatGames(e.gamesUs.filter(_.won).map(_.weight).sum)),
      Vector("WinPct")   ++ evaluations.map(e => formatPercentage(e.winrateVsEnemy)),
      Vector("WinEst")   ++ evaluations.map(e => formatPercentage(e.probabilityWin)))
    columns.map(_.mkString("\t")).foreach(tell)
  }
  private def logStrategyInterest(): Unit = {
    tell("Strategy interest")
    With.strategy.winProbabilityByBranchLegal
      .toVector
      .sortBy( - _._2)
      .map(pair => (formatPercentage(pair._2), pair._1.toSeq.map(_.toString).sorted.mkString(" + ")))
      .map(p => p._1 + " " + p._2)
      .foreach(tell)
  }
  private def formatGames(games: Double): String = "%1.1f".format(games)
  private def formatPercentage(value: Double): String = (value * 100.0).toInt + """%%"""

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

