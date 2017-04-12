package Performance.Systems

import Lifecycle.With

class SystemUnitTracking extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyUnitTracking
  
  override protected def onRun() {
    With.units.update()
  }
}
