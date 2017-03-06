package Plans.Macro.Automatic

import Plans.Allocation.LockUnits
import Plans.Plan
import Startup.With
import Strategies.UnitCounters.UnitCountExactly
import Strategies.UnitMatchers.UnitMatchWorker
import Types.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class GatherGas extends Plan {
  
  val unitCounter = new UnitCountExactly(3)
  val drillers = new LockUnits
  drillers.unitMatcher.set(UnitMatchWorker)
  drillers.unitCounter.set(unitCounter)
  
  override def getChildren: Iterable[Plan] = List(drillers)
  
  override def onFrame() {
    unitCounter.maximum.set(_getMinerCount)
    drillers.onFrame()
    _orderWorkers()
  }
  
  def _ourRefineries:Iterable[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.complete && unit.utype.isRefinery)
  
  def _getMinerCount:Int = {
    //TODO: Stop taking guys off gas if we're saturated on minerals
    if (With.game.self.gas > Math.max(200, With.game.self.minerals)) {
      return 0
    }
    var workers = 3 * _ourRefineries.size
    workers = Math.min(workers, With.units.ours.filter(unit => unit.complete && unit.utype.isWorker).size / 3)
    workers
  }
  
  def _orderWorkers() {
    val unassignedWorkers = new mutable.HashSet[FriendlyUnitInfo] ++= drillers.units
    _ourRefineries
      .foreach(refinery => {
        (1 to 3).foreach(i => {
          if (unassignedWorkers.nonEmpty) {
            val worker = unassignedWorkers.minBy(_.position.getApproxDistance(refinery.position))
            unassignedWorkers.remove(worker)
            if ( ! worker.isGatheringGas) {
              worker.baseUnit.gather(refinery.baseUnit)
            }
          }
        })
      })
  }
}
