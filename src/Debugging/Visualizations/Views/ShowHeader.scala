package Debugging.Visualizations.Views

import Lifecycle.{PurpleBWClient, With}

object ShowHeader extends DebugView {
  
  override def renderScreen (): Unit = {
    val totalSeconds = With.frame * 42 / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val clock = f"${minutes}:${"%02d".format(seconds)}"

    val h = With.visualization.lineHeightSmall
    With.game.drawTextScreen(5,   1 * h, f"${With.game.getLatencyFrames} latency frames")
    With.game.drawTextScreen(80,  1 * h, f"${With.latency.turnSize} frames/turn")
    With.game.drawTextScreen(155, 1 * h, f"${With.performance.frameMeanMs}ms avg")
    With.game.drawTextScreen(230, 1 * h, f"${With.performance.frameMaxMs}ms max")
    With.game.drawTextScreen(305, 1 * h, f"${PurpleBWClient.framesBehind} frames back")
    With.game.drawTextScreen(375, 1 * h, clock)
    With.game.drawTextScreen(405, 1 * h, f"${With.frame}")
    With.game.drawTextScreen(5,   2 * h, f"+${With.configuration.frameLimitMs}ms: ${With.performance.framesOverShort}/320")
    With.game.drawTextScreen(80,  2 * h, f"+1000ms: ${With.performance.framesOver1000}/10")
    With.game.drawTextScreen(155, 2 * h, f"+10000ms: ${With.performance.framesOver10000}/1")
    With.game.drawTextScreen(230, 2 * h, With.blackboard.status.get.mkString(", "))
    With.game.drawTextScreen(5,   3 * h, f"${With.strategy.selected.map(_.toString).mkString(" ")} ${if (With.fingerprints.status.nonEmpty) " | " else ""} ${With.fingerprints.status.mkString(" ").replaceAll("Finger", "")}")
  }
}
