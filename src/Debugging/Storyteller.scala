package Debugging

import Debugging.Visualizations.Views.Planning.ShowStrategyEvaluations
import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass

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

  val stories: Seq[Story] = Seq[Story](
    Story("Strategy", () => With.strategy.selectedCurrently.map(_.toString).mkString(" ")),
    Story("Status", () => With.blackboard.status.get.mkString(", ")),
    Story("Enemy race", () => With.enemy.raceCurrent.toString),
    Story("Fingerprints", () => With.fingerprints.status),
    Story("Our bases", () => With.geography.ourBases.size.toString),
    Story("Enemy bases", () => With.geography.enemyBases.size.toString)
  )

  def onFrame(): Unit = {
    stories.foreach(_.update())
    logIntelligence()
  }

  var unitsBefore: Set[UnitClass] = Set.empty
  private def logIntelligence(): Unit = {
    val unitsAfter = With.enemies.flatMap(With.intelligence.unitsShown.all(_).view).filter(_._2.nonEmpty).map(_._1).toSet
    val unitsDiff = unitsAfter -- unitsBefore
    if (unitsDiff.nonEmpty) {
      With.logger.debug("New enemy units: " + unitsDiff.map(_.toString).mkString(", "))
    }
    unitsBefore = unitsAfter
  }

  private def logPerformance() {
    With.logger.debug(Seq(
      Seq(With.performance.frameLimitShort, With.performance.framesOverShort),
      Seq(1000, With.performance.framesOver1000),
      Seq(10000, With.performance.framesOver10000)).map(line => line.head.toString + "ms: " + line.last.toString).mkString("\n"))
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
