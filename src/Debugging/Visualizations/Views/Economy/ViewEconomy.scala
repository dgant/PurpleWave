package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Views.View

object ViewEconomy extends View {
  
  def render() {
    VisualizeEconomy.render()
    VisualizeScheduler.render()
  }
}
