package Performance.Systems

import Lifecycle.With

class SystemGrids extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyGrids
  
  override protected def onRun() {
    With.grids.update()
  }
}
