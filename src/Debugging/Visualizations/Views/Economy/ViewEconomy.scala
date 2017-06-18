package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Views.View

object ViewEconomy extends View {
  
  def render() {
    ScreenEconomy.render()
    ScreenScheduler.render()
  }
}
