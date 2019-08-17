package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowPerformanceSummary extends View {
  
  override def renderScreen() {
    DrawScreen.header(5,  With.game.getLatencyFrames              + " latency frames")
    DrawScreen.header(80, With.latency.turnSize                   + " frames/turn")
    DrawScreen.header(155, With.performance.meanFrameMilliseconds + "ms avg")
    DrawScreen.header(230, With.performance.maxFrameMilliseconds  + "ms max")
    With.game.drawTextScreen(5,   2 * With.visualization.lineHeightSmall, "+55ms: "     + With.performance.framesOverShort  + "/320")
    With.game.drawTextScreen(80,  2 * With.visualization.lineHeightSmall, "+1000ms: "   + With.performance.framesOver1000   + "/10")
    With.game.drawTextScreen(155, 2 * With.visualization.lineHeightSmall, "+10000ms: "  + With.performance.framesOver10000  + "/1")
    if (With.performance.disqualified) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Large)
      DrawScreen.header(295, ":/")
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    }
  }
}
