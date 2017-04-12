package Performance.Systems

import Lifecycle.With

class SystemLatency extends AbstractSystem {
  
  override def skippable: Boolean = false
  
  override protected def onRun() {
    With.latency.onFrame()
  }
}
