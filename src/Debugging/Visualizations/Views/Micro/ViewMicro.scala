package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View

object ViewMicro extends View {
  
  def render() {
    VisualizeGrids.render()
    VisualizeHitPoints.render()
    VisualizeMovementHeuristics.render()
    VisualizeUnitsForeign.render()
    VisualizeUnitsOurs.render()
  }
}
