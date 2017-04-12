package Performance.Systems

import Lifecycle.With

class SystemGeography extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyGeography
  
  override protected def onRun(): Unit = {
   With.geography.update()
  }
}
