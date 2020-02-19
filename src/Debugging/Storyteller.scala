package Debugging

import Debugging.Visualizations.Views.Planning.ShowStrategyEvaluations
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrades
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

class Storyteller {

  case class Story(label: String, currentValue: () => String, var valueLast: String = "") {
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
    Story("Opponents",      () => With.enemies.filter(_.isEnemy).map(_.name).mkString(", ")),
    Story("Playbook",       () => With.configuration.playbook.toString),
    Story("Policy",         () => With.configuration.playbook.strategySelectionPolicy.toString),
    Story("Strategy",       () => With.strategy.selectedCurrently.map(_.toString).mkString(" ")),
    Story("Status",         () => With.blackboard.status.get.mkString(", ")),
    Story("Enemy race",     () => With.enemy.raceCurrent.toString),
    Story("Fingerprints",   () => With.fingerprints.status.mkString(" ")),
    Story("Our bases",      () => With.geography.ourBases.size.toString),
    Story("Enemy bases",    () => With.geography.enemyBases.size.toString),
    Story("Our techs",      () => interestingTechs.view.filter(With.self.hasTech).mkString(", ")),
    Story("Enemy techs",    () => interestingTechs.view.filter(t => With.enemies.exists(_.hasTech(t))).mkString(", ")),
    Story("Our upgrades",   () => Upgrades.all.view.map(u => (u, With.self.getUpgradeLevel(u))).filter(_._2 > 0).map(u => u._1 + " = " + u._2).mkString(", ")),
    Story("Enemy upgrades", () => Upgrades.all.view.map(u => (u, ByOption.max(With.enemies.map(_.getUpgradeLevel(u))).getOrElse(0))).filter(_._2 > 0).map(u => u._1 + " = " + u._2).mkString(", "))
  )

  def onFrame(): Unit = {
    stories.foreach(_.update())
    logIntelligence()
  }

  var unitsBefore: Set[UnitClass] = Set.empty
  private def logIntelligence(): Unit = {
    val unitsAfter = With.enemies.flatMap(With.intelligence.unitsShown.all(_).view).filter(_._2.nonEmpty).map(_._1).toSet
    val unitsDiff = unitsAfter -- unitsBefore
    unitsDiff.foreach(newType => {
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
    unitsBefore = unitsAfter
  }

  private def logPerformance() {
    With.logger.debug(Seq(
      Seq(With.performance.frameLimitShort, With.performance.framesOverShort),
      Seq(1000, With.performance.framesOver1000),
      Seq(10000, With.performance.framesOver10000)).map(line => line.head.toString + "ms: " + line.last.toString).mkString("\n"))
    With.logger.debug("Our performance was " + (if (With.performance.disqualified) "BAD" else if (With.performance.danger) "DANGEROUS" else "good"))
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

  def onEnd(): Unit = {
    logPerformance()
    logStrategyEvaluation()
  }
}
