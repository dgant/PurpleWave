package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridMobility extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.mobilityTerrain.update()
  
}
