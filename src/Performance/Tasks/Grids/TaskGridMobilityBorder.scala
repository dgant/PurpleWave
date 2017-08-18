package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridMobilityBorder extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.mobilityBorder.update()
  
}
