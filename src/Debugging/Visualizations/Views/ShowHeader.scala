package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawScreen
import Information.Geography.NeoGeo.MapIdentifier
import Lifecycle.{PurpleBWClient, With}

object ShowHeader extends DebugView {
  
  override def renderScreen (): Unit = {
    val totalSeconds = With.frame * 42 / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val clock = f"${minutes}:${"%02d".format(seconds)}"

    val h = With.visualization.lineHeightSmall
    DrawScreen.text(5,   1 * h, f"LF ${With.game.getLatencyFrames}/${With.latency.turnSize}")
    DrawScreen.text(45,  1 * h, f"${With.performance.frameMeanMs}ms avg - ${With.performance.frameMaxMs}ms max - ${-PurpleBWClient.framesBehind}f")
    DrawScreen.text(165, 1 * h, clock)
    DrawScreen.text(205, 1 * h, f"${With.frame}")
    DrawScreen.text(245, 1 * h, f"${With.configuration.frameLimitMs}ms: ${With.performance.framesOverShort}/320 ${With.performance.framesOver1000}/10 ${With.performance.framesOver10000}/1")
    DrawScreen.text(365, 1 * h, f"A:${With.reaction.agencyAverage} C:${With.reaction.clusteringAverage} P:${With.reaction.planningAverage}")
    DrawScreen.text(5,   2 * h, MapIdentifier(With.game.mapFileName))
    DrawScreen.text(125, 2 * h, f"${With.strategy.strategiesSelected.map(_.toString).mkString(" ")} ${if (With.fingerprints.status.nonEmpty) " | " else ""} ${With.fingerprints.status.mkString(" ").replaceAll("Finger", "")}")
    DrawScreen.text(5,   3 * h, With.blackboard.status.get.mkString(", "))
  }
}
