package Debugging.Visualizations.Views

import Information.Geography.NeoGeo.MapIdentifier
import Lifecycle.{PurpleBWClient, With}

object ShowHeader extends DebugView {
  
  override def renderScreen (): Unit = {
    val totalSeconds = With.frame * 42 / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val clock = f"${minutes}:${"%02d".format(seconds)}"

    val h = With.visualization.lineHeightSmall
    With.game.drawTextScreen(5,   1 * h, f"LF ${With.game.getLatencyFrames}/${With.latency.turnSize}")
    With.game.drawTextScreen(45,  1 * h, f"${With.performance.frameMeanMs}ms avg - ${With.performance.frameMaxMs}ms max - ${-PurpleBWClient.framesBehind}f")
    With.game.drawTextScreen(165, 1 * h, clock)
    With.game.drawTextScreen(205, 1 * h, f"${With.frame}")
    With.game.drawTextScreen(245, 1 * h, f"${With.configuration.frameLimitMs}ms: ${With.performance.framesOverShort}/320 ${With.performance.framesOver1000}/10 ${With.performance.framesOver10000}/1")
    With.game.drawTextScreen(5,   2 * h, MapIdentifier(With.game.mapFileName))
    With.game.drawTextScreen(125, 2 * h, f"${With.strategy.selected.map(_.toString).mkString(" ")} ${if (With.fingerprints.status.nonEmpty) " | " else ""} ${With.fingerprints.status.mkString(" ").replaceAll("Finger", "")}")
    With.game.drawTextScreen(5,   3 * h, With.blackboard.status.get.mkString(", "))
  }
}
