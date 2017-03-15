package Planning.Plans.Macro.Automatic

import Planning.Plans.Allocation.LockUnits
import Planning.Plan
import Startup.With
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.UnitMatchWorker
import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class GatherGas extends Plan {
  
  val unitCounter = new UnitCountBetween(1, 3)
  val drillers = new LockUnits
  drillers.unitMatcher.set(UnitMatchWorker)
  drillers.unitCounter.set(unitCounter)
  
  override def getChildren: Iterable[Plan] = List(drillers)
  
  override def onFrame() {
    unitCounter.maximum.set(_idealMinerCount)
    drillers.onFrame()
    _orderWorkers()
  }
  
  def _ourRefineries:Iterable[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.complete && unit.isGas)
  
  def _idealMinerCount:Int = {
    //TODO: Stop taking guys off gas if we're saturated on minerals
    if (With.self.gas > Math.max(200, With.self.minerals)) {
      return 0
    }
    var maxDrillers = 3 * _ourRefineries.size
    maxDrillers = Math.min(maxDrillers, With.units.ours.filter(unit => unit.complete && unit.utype.isWorker).size / 3)
    maxDrillers
  }
  
  def _orderWorkers() {
    val availableDrillers = new mutable.HashSet[FriendlyUnitInfo] ++= drillers.units
    _ourRefineries
      .toList
      .sortBy(-_.gasLeft)
      .foreach(refinery =>
        (1 to 3).foreach(i => {
          if (availableDrillers.nonEmpty) {
            val driller = availableDrillers.minBy(_.pixel.getApproxDistance(refinery.pixel))
            availableDrillers.remove(driller)
            if ( ! driller.isGatheringGas || driller.distance(refinery) > 32 * 8) {
              driller.baseUnit.gather(refinery.baseUnit)
            }}}))
  }
}
