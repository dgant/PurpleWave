package Performance.Systems

import Lifecycle.With

class SystemCamera extends AbstractSystem {
  
  override protected def onRun() {
    With.camera.onFrame()
  }
}
