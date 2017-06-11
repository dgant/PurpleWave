package Debugging.Visualizations.Views.Planning

import Lifecycle.With
import Planning.Plan

object VisualizePlansMap {
  
  def render() {
    visualizePlansRecursively(With.gameplan)
  }
  
  private def visualizePlansRecursively(plan: Plan) {
    plan.visualize()
    plan.getChildren.filter(isRelevant).foreach(visualizePlansRecursively)
  }
  
  private def isRelevant(plan: Plan): Boolean = {
    With.prioritizer.isPrioritized(plan) && ! plan.isComplete
  }
}
