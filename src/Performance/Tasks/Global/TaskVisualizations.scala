package Performance.Tasks.Global

import Debugging.Visualizations.Visualization
import Performance.Tasks.AbstractTask

class TaskVisualizations extends AbstractTask {
  
  override def skippable  : Boolean = false
  
  override protected def onRun() {
    Visualization.render()
  }
}
