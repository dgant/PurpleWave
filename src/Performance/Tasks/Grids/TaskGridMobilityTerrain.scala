package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridMobilityTerrain extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.mobilityTerrain.update()
  
}
