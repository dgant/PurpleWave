package Performance

import Lifecycle.With
import Performance.Systems._

class SystemQueue {
  
  val systems = Vector(
    new SystemBattles,
    new SystemEconomy,
    new SystemGrids,
    new SystemManners,
    new SystemMicro,
    new SystemPlanning,
    new SystemUnitTracking
  )
  def onFrame() {
    systems.sortBy(system => - system.urgency * system.framesSinceRunning)
    
    systems
      .foreach(system =>
        if (With.frame == 0
          ||system == systems.head
          || With.performance.millisecondsLeft > system.maxRunMilliseconds)
          system.run)
  }
}
