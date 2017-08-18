package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridMobilityBuildings extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.mobilityBuildings.update()
  
}
