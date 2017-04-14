package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridBuildable extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.buildable.update()
  
}
