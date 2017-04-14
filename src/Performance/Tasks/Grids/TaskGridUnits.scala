package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridUnits extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.units.update()
  
}
