package Debugging.Visualizations.Views

import Lifecycle.With

abstract class View {
  
  lazy val name: String = getClass.getSimpleName.replace("$", "").replace("Show", "")
  
  def renderScreen(): Unit = {}
  def renderMap(): Unit = {}
  
  def inUse: Boolean = (
    With.visualization.enabled
    && (With.visualization.map || With.visualization.screen)
    && With.visualization.views.contains(this)
  )
  
  def mapInUse: Boolean = {
    inUse && With.visualization.map
  }
  
  def screenInUse: Boolean = {
    inUse && With.visualization.screen
  }
}
