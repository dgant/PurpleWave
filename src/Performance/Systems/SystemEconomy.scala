package Performance.Systems

import Lifecycle.With

class SystemEconomy extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyEconomy
  
  override protected def onRun(): Unit = {
    With.economy.update()
  }
}
