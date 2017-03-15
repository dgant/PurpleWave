package Development.Visualization

import Startup.With

object VisualizePerformance {
  def render() {
    DrawScreen.header(125, With.latency.minTurnSize         + " frames/turn")
    DrawScreen.header(200, With.performance.meanFrameLength + "ms avg")
    DrawScreen.header(275, With.performance.maxFrameLength  + "ms max")
    DrawScreen.header(350, With.performance.frameDelay(1)   + " cache duration")
  }
}
