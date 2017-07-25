package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridCreep extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.creep.update()
  
}
