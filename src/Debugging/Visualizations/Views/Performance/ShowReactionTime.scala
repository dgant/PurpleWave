package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowReactionTime extends View {
  
  override def renderScreen() {
    DrawScreen.table(
      5, 5 * With.visualization.lineHeightSmall,
      Vector(
        Vector("", "Agency", "Clustering", "Estimation", "Planning", "Grid units"),
        Vector("Samples:",    With.agents.cycleLengths.size.toString,   With.battles.clustering.runtimes.size.toString, With.battles.estimationRuntimes.size.toString,  With.prioritizer.frameDelays.size.toString, With.grids.units.updateIntervals.size.toString),
        Vector("Last:",       With.reaction.agencyLast.toString,        With.reaction.clusteringLast.toString,          With.reaction.estimationLast.toString,          With.reaction.planningLast.toString),
        Vector("Max:",        With.reaction.agencyMax.toString,         With.reaction.clusteringMax.toString,           With.reaction.estimationMax.toString,           With.reaction.planningMax.toString),
        Vector("Avg:",        With.reaction.agencyAverage.toString,     With.reaction.clusteringAverage.toString,       With.reaction.estimationAverage.toString,       With.reaction.planningAverage.toString),
        Vector("Avg Total:",  With.reaction.framesTotal.toString)))
  }
}
