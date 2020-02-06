package Debugging

import Lifecycle.With

class Storyteller {

  case class Story(label: String, currentValue: () => String, var valueLast: String = "") {
    def update(): Unit = {
      val valueNew = currentValue()
      if (valueLast != valueNew) {
        With.logger.debug(label + (if (valueLast == "") " is " else " changed from ") + valueLast + " to " + valueNew)
      }
      valueLast = valueNew
    }
  }

  val stories = Seq[Story](
    Story("Strategy", () => With.strategy.selectedCurrently.map(_.toString).mkString(" ")),
    Story("Status", () => With.blackboard.status.get.mkString(", ")),
    Story("Enemy race", () => With.enemy.raceCurrent.toString),
    Story("Fingerprints", () => With.fingerprints.status))

  def onFrame(): Unit = {
    stories.foreach(_.update())
  }

  def onEnd(): Unit = {
    With.logger.debug(Seq(
      Seq(With.performance.frameLimitShort, With.performance.framesOverShort),
      Seq(1000, With.performance.framesOver1000),
      Seq(10000, With.performance.framesOver10000)).map(line => line.head.toString + "ms: " + line.last.toString).mkString("\n"))
  }
}
