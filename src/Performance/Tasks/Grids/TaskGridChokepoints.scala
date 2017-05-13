package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridChokepoints extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.chokepoints.update()
  
}
