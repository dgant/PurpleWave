package Performance.Systems

import Debugging.Visualizations.Visualization

class SystemVisualizations extends AbstractSystem {
  
  override def skippable  : Boolean = false
  
  override protected def onRun() {
    Visualization.render()
  }
}
