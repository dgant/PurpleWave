package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridBuildableTerrain extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.buildableTerrain.update()
  
}
