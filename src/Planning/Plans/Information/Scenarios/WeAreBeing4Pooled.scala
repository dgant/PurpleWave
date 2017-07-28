package Planning.Plans.Information.Scenarios

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class WeAreBeing4Pooled extends Plan {
  
  var conditionsMet = false
  
  override def isComplete: Boolean = conditionsMet
  
  override def onUpdate(): Unit = {
    if (With.frame > 24 * 60 * 4) {
      conditionsMet = false
      return
    }
  
    // 5 pool spawning pool finishes about 1:35 == 95 seconds
    // Let's add a bit of buffer to our detection.
    //
    val scaryThresholdZerglingArrival         = 24 * (60 * 2 + 30)
    val scaryThresholdDroneArrival            = 24 * (60 * 2 + 15)
    val scaryThresholdSpawningPoolCompletion  = 24 * (60 + 40)
    
    lazy val zerglings     = With.units.enemy.filter(_.is(Zerg.Zergling))
    lazy val drones        = With.units.enemy.filter(_.is(Zerg.Drone))
    lazy val spawningPools = With.units.enemy.filter(_.is(Zerg.SpawningPool))
  
    lazy val zerglingArrival         = if (zerglings.isEmpty)      Int.MaxValue else zerglings.map(arrivalFrame).min
    lazy val droneArrival            = if (drones.isEmpty)         Int.MaxValue else drones.map(arrivalFrame).min
    lazy val spawningPoolCompletion  = if (spawningPools.isEmpty)  Int.MaxValue else spawningPools.map(_.completionFrame).min
  
    lazy val scaryZerglings      = zerglingArrival        < scaryThresholdZerglingArrival
    lazy val scaryDrone          = droneArrival           < scaryThresholdDroneArrival
    lazy val scarySpawningPool   = spawningPoolCompletion < scaryThresholdSpawningPoolCompletion
    
    conditionsMet = conditionsMet || scaryZerglings || scaryDrone || scarySpawningPool
  }
  
  def arrivalFrame(unit: ForeignUnitInfo): Int = {
    if (With.geography.ourBases.nonEmpty)
      With.geography.ourBases.map(base => unit.framesToTravelTo(base.townHallTile.pixelCenter)).min
    else
      unit.framesToTravelTo(With.geography.home.pixelCenter)
  }
}
