package Performance

import Lifecycle.With
import Performance.Systems._

class SystemQueue {
  
  val systems = Vector(
    new SystemUnitTracking,
    new SystemGeography,
    new SystemGrids,
    new SystemBattles,
    new SystemEconomy,
    new SystemPlanning,
    new SystemMicro,
    new SystemManners
  )
  
  def onFrame() {
    if (With.frame == 0) {
      systems.foreach(_.run())
    } else {
      var definitelyRunNextSystem = true
      systems
        .sortBy(system => - system.urgency * system.framesSinceRunning)
        .foreach(system =>
          if (definitelyRunNextSystem  || With.performance.millisecondsLeftThisFrame > system.runMillisecondsMax) {
            definitelyRunNextSystem = false
            system.run()
          })
    }
  }
}
