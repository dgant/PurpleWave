package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridFriendlyVision extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.friendlyVision.update()
  
}
