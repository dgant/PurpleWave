package Debugging.Visualizations.Views

import Lifecycle.With

abstract class View {
  
  private def unimplemented() {}
  
  def renderScreen(): Unit = unimplemented()
  def renderMap(): Unit = unimplemented()
  
  def inUse: Boolean = {
    With.visualization.enabled &&
      With.visualization.views.contains(this) &&
      (With.visualization.map || With.visualization.screen)
  }
}
