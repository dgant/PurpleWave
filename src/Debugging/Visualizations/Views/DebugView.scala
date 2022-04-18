package Debugging.Visualizations.Views

import Debugging.ToString
import Lifecycle.With

abstract class DebugView {
  
  lazy val name: String = ToString(this).replace("Show", "")
  
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
