package Planning.Plans.Macro.Automatic

import Micro.Intentions.Intention
import Planning.Plans.Allocation.LockUnits
import Planning.Plan
import Startup.With
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.UnitMatchWorker
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class GatherGas extends Plan {
  
  private val unitCounter = new UnitCountBetween(1, 3)
  private val drillers = new LockUnits
  drillers.unitMatcher.set(UnitMatchWorker)
  drillers.unitCounter.set(unitCounter)
  
  override def getChildren: Iterable[Plan] = List(drillers)
  
  override def onFrame() {
    unitCounter.maximum.set(idealMinerCount)
    drillers.onFrame()
    orderWorkers()
  }
  
  private def ourRefineries:Iterable[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.complete && unit.unitClass.isGas)
  
  private def idealMinerCount:Int = {
    //TODO: Stop taking guys off gas if we're saturated on minerals
    if (With.self.gas > Math.max(200, With.self.minerals)) {
      return 0
    }
    var maxDrillers = 3 * ourRefineries.size
    maxDrillers = Math.min(maxDrillers, With.units.ours.filter(unit => unit.complete && unit.unitClass.isWorker).size / 3)
    maxDrillers
  }
  
  private def orderWorkers() {
    val availableDrillers = new mutable.HashSet[FriendlyUnitInfo] ++= drillers.units
    ourRefineries
      .toList
      .sortBy(-_.gasLeft)
      .foreach(refinery =>
        (1 to 3).foreach(i => {
          if (availableDrillers.nonEmpty) {
            val driller = availableDrillers.minBy(_.pixelCenter.getApproxDistance(refinery.pixelCenter))
            availableDrillers.remove(driller)
            if ( ! driller.gatheringGas || driller.pixelDistance(refinery) > 32 * 8) {
              With.executor.intend(new Intention(this, driller) { toGather = Some(refinery) })
            }}}))
  }
}
