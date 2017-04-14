package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridWalkableTerrain extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.walkableTerrain.update()
  
}
