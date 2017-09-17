package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Performance.MicroReaction
import bwapi.Color

object ShowReactionTime extends View {
  
  override def renderMap() {
    (With.units.ours ++ With.units.enemy).toSeq.foreach(unit => {
      val radius = Math.min(unit.unitClass.width, unit.unitClass.height)/2
      if (unit.battle.isEmpty) {
        DrawMap.circle(unit.pixelCenter, radius, Color.Black, solid = true)
      }
      val me = unit.friendly
      if (me.isDefined) {
        val delay = With.framesSince(me.get.agent.lastFrame)
        val ratio = Math.min(1.0, delay.toDouble / MicroReaction.agencyMax)
        val width = (ratio * radius).toInt
        if (width > 0) {
          DrawMap.circle(unit.pixelCenter, width, Colors.BrightRed, solid = true)
        }
      }
    })
  }
  
  override def renderScreen() {
    DrawScreen.header(5,  With.game.getLatencyFrames              + " latency frames")
    DrawScreen.header(80, With.latency.turnSize                   + " frames/turn")
    DrawScreen.header(155, With.performance.meanFrameMilliseconds + "ms avg")
    DrawScreen.header(230, With.performance.maxFrameMilliseconds  + "ms max")
    With.game.drawTextScreen(5,   2 * With.visualization.lineHeightSmall, "+85ms: "     + With.performance.framesOver85     + "/320")
    With.game.drawTextScreen(80,  2 * With.visualization.lineHeightSmall, "+1000ms: "   + With.performance.framesOver1000   + "/10")
    With.game.drawTextScreen(155, 2 * With.visualization.lineHeightSmall, "+10000ms: "  + With.performance.framesOver10000  + "/1")
    if (With.performance.disqualified) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Large)
      //With.game.drawTextScreen(230, 2 * With.visualization.lineHeightSmall, "Disqualified!")
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    }
    
    DrawScreen.table(
      0, 7 * With.visualization.lineHeightSmall,
      Vector(
        Vector("", "Agency", "Clustering"),
        Vector("Last:",       MicroReaction.agencyLast.toString,    MicroReaction.battlesLast.toString),
        Vector("Max:",        MicroReaction.agencyMax.toString,     MicroReaction.battlesMax.toString),
        Vector("Avg:",        MicroReaction.agencyAverage.toString, MicroReaction.battlesAverage.toString),
        Vector("Avg Total:",  MicroReaction.framesTotal.toString)))
  }
}
