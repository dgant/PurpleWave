package Performance

import Lifecycle.With
import Performance.Systems._

class SystemQueue {
  
  val systems = Vector(
    new SystemLatency,
    new SystemUnitTracking,
    new SystemGeography,
    new SystemGrids,
    new SystemBattleClassify,
    new SystemBattleAssess,
    new SystemEconomy,
    new SystemPlanning,
    new SystemMicro,
    new SystemManners,
    new SystemCamera,
    new SystemVisualizations
  )
  
  def onFrame() {
    if (With.frame == 0) {
      systems.foreach(_.run())
    } else {
      var definitelyRunNextSystem = true
      systems
        .sortBy(system => - system.urgency * system.framesSinceRunning)
        .sortBy(system => system.skippable)
        .foreach(system =>
          if (definitelyRunNextSystem || ! system.skippable || With.performance.millisecondsLeftThisFrame > system.runMillisecondsMax) {
            if (system.skippable) {
              definitelyRunNextSystem  = false
            }
            system.run()
          } else {
            system.skip()
          })
    }
  }
}
