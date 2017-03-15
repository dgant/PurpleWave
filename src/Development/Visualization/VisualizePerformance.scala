package Development.Visualization

import Startup.With

object VisualizePerformance {
  def render() {
    With.game.drawTextScreen(125, 5, With.latency.minTurnSize         + " frames/turn")
    With.game.drawTextScreen(200, 5, With.performance.meanFrameLength + "ms avg")
    With.game.drawTextScreen(275, 5, With.performance.maxFrameLength  + "ms max")
    With.game.drawTextScreen(350, 5, With.performance.frameDelay(1)   + "-frame caching")
  }
}
