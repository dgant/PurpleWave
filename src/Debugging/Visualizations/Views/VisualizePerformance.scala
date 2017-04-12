package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object VisualizePerformance {
  def render() {
    DrawScreen.header(50,  With.game.getLatencyFrames       + " latency frames")
    DrawScreen.header(125, With.latency.turnSize            + " frames/turn")
    DrawScreen.header(200, With.performance.meanFrameLength + "ms avg")
    DrawScreen.header(275, With.performance.maxFrameLength  + "ms max")
    DrawScreen.header(350, With.performance.frameDelay(1)   + " cache duration")
  }
}
