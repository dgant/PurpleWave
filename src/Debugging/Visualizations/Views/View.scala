package Debugging.Visualizations.Views

import Lifecycle.With

abstract class View {
  
  lazy val name: String = getClass.toString.replace("$", "").replace("Show", "")
  
  def renderScreen(): Unit = {}
  def renderMap(): Unit = {}
  
  def inUse: Boolean = {
    With.visualization.enabled &&
    With.visualization.views.contains(this) &&
    (With.visualization.map || With.visualization.screen)
  }
}
