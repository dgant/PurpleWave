package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskCamera extends AbstractTask {
  
  override protected def onRun() {
    With.camera.onFrame()
  }
}
