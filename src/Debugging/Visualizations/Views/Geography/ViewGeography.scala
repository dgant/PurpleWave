package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Views.View

object ViewGeography extends View {
  
  def render() {
    VisualizeGeography.render()
    VisualizeChokepoints.render()
    VisualizeBases.render()
    VisualizeRealEstate.render()
  }
}
