package Performance.Systems

import Lifecycle.With

class SystemBattles extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyBattles
  
  override protected def onRun(): Unit = {
    With.battles.update()
  }
}
