package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View

object ViewPlanning extends View {
  
  def render() {
    VisualizeResources.render()
    VisualizePlansScreen.render()
  }
}
