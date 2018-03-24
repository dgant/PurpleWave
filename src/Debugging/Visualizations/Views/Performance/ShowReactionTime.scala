package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import bwapi.Color

object ShowReactionTime extends View {
  
  override def renderMap() {
    return
    (With.units.ours ++ With.units.enemy).toSeq.foreach(unit => {
      val radius = unit.unitClass.dimensionMin
      
      if (unit.battle.isEmpty) {
        DrawMap.circle(unit.pixelCenter, radius, Color.Black, solid = true)
      }
      else if ( ! With.battles.byUnit.contains(unit)) {
        DrawMap.circle(unit.pixelCenter, radius, Colors.NeonBlue, solid = true)
      }
      val me = unit.friendly
      if (me.isDefined) {
        val delay = With.framesSince(me.get.agent.lastFrame)
        val ratio = Math.min(1.0, delay.toDouble / With.reaction.agencyMax)
        val width = (ratio * radius).toInt
        if (width > 0) {
          DrawMap.circle(unit.pixelCenter, width, Colors.BrightRed, solid = true)
        }
        if (With.framesSince(unit.frameDiscovered) < 72) {
          DrawMap.label(With.framesSince(unit.frameDiscovered).toString, unit.pixelCenter)
        }
      }
    })
  }
  
  override def renderScreen() {
    DrawScreen.table(
      5, 5 * With.visualization.lineHeightSmall,
      Vector(
        Vector("", "Agency", "Clustering"),
        Vector("Samples:",    With.agents.runtimes.size.toString,   With.battles.clustering.runtimes.size.toString),
        Vector("Last:",       With.reaction.agencyLast.toString,    With.reaction.clusteringLast.toString),
        Vector("Max:",        With.reaction.agencyMax.toString,     With.reaction.clusteringMax.toString),
        Vector("Avg:",        With.reaction.agencyAverage.toString, With.reaction.clusteringAverage.toString),
        Vector("Avg Total:",  With.reaction.framesTotal.toString)))
  }
}
