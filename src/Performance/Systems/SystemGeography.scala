package Performance.Systems

import Lifecycle.With

class SystemGeography extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyGeography
  
  override protected def onRun() {
   With.geography.update()
  }
}
