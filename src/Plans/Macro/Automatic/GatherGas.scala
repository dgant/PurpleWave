package Plans.Macro.Automatic

import Plans.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property

import scala.collection.mutable

class GatherGas extends Plan {
  
  val _workerPlan = new LockUnitsExactly { unitMatcher.set(UnitMatchWorker); quantity.set(3) }
  val workerPlan = new Property[LockUnits](_workerPlan)
  
  override def getChildren: Iterable[Plan] = { List(workerPlan.get) }
  
  override def onFrame() {
    _workerPlan.quantity.set(_getMinerCount)
    workerPlan.get.onFrame()
    _orderWorkers()
  }
  
  def _ourRefineries:Iterable[FriendlyUnitInfo] = {
    With.units.ours.filter(unit => unit.complete && unit.utype.isRefinery)
  }
  
  def _getMinerCount:Int = {
    //TODO: Stop taking guys off gas if we're saturated on minerals
    if (With.game.self.gas > Math.max(400, With.game.self.minerals)) {
      return 0
    }
    var workers = 3 * _ourRefineries.size
    workers = Math.min(workers, With.units.ours.filter(unit => unit.complete && unit.utype.isWorker).size / 3)
    workers
  }
  
  def _orderWorkers() {
    val unassignedWorkers = new mutable.HashSet[FriendlyUnitInfo] ++= workerPlan.get.units
    _ourRefineries
      .foreach(refinery => {
        (1 to 3).foreach(i => {
          if (unassignedWorkers.nonEmpty) {
            val worker = unassignedWorkers.minBy(_.position.getApproxDistance(refinery.position))
            unassignedWorkers.remove(worker)
            if ( ! worker.isGatheringGas) {
              //Workers tend to get stuck returning cargo
              /*
              if (worker.isCarryingMinerals || worker.isCarryingGas) {
                
                //Can't spam return cargo
                if (worker.getLastCommand.getUnitCommandType != UnitCommandType.Return_Cargo  || worker.getLastCommandFrame < With.game.getFrameCount - 24) {
                  worker.returnCargo()
                }
              }
              else*/ {
                worker.baseUnit.gather(refinery.baseUnit)
              }
            }
          }
        })
      })
  }
}
