package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridWalkable extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.walkable.update()
  
}
