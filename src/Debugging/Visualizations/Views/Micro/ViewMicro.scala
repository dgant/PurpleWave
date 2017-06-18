package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View

object ViewMicro extends View {
  
  def render() {
    MapUnits.render()
    MapUnitsForeign.render()
    MapUnitsOurs.render()
    MapMovementHeuristics.render()
  }
}
