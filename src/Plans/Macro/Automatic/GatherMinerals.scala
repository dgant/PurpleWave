package Plans.Macro.Automatic

import Plans.Allocation.{LockUnits, LockUnitsGreedily}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker
import Types.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo}
import Utilities.Enrichment.EnrichUnitType._
import Utilities.{Limiter, Property}
import bwapi.UnitCommandType

import scala.collection.mutable

class GatherMinerals extends Plan {
  
  val workerPlan = new Property[LockUnits](new LockUnitsGreedily { unitMatcher.set(UnitMatchWorker) })
  
  override def getChildren: Iterable[Plan] = { List(workerPlan.get) }
  
  var _minerals:List[ForeignUnitInfo] = List.empty
  val _workersByMineral = new mutable.HashMap[ForeignUnitInfo, mutable.HashSet[FriendlyUnitInfo]] {
    override def default(key: ForeignUnitInfo): mutable.HashSet[FriendlyUnitInfo] = { put(key, mutable.HashSet.empty); this(key)}}
  val _mineralByWorker = new mutable.HashMap[FriendlyUnitInfo, ForeignUnitInfo]
  
  val _limitResetAssignments = new Limiter(24 * 5, _resetAssignments)
  override def onFrame() {
    workerPlan.get.onFrame()
    _limitResetAssignments.act()
    val workers = workerPlan.get.units
    _mineralByWorker.keySet.diff(workers).foreach(_assignWorker)
    workers
      .filterNot(worker => worker.isGatheringMinerals)
      .foreach(_orderWorker)
  }
  
  def _resetAssignments() {
    val ourTownHalls = _getTownHalls
    if (ourTownHalls.isEmpty) { return }
    _mineralByWorker.clear()
    _workersByMineral.clear()
    _minerals = With.units.neutral
      .filter(_.isMinerals)
      .filter(mineral => With.geography.ourHarvestingAreas.exists(_.contains(mineral.tilePosition)))
      .toList
  }
  
  def _getTownHalls:Iterable[FriendlyUnitInfo] = {
    With.units.ours.filter(unit => unit.complete && unit.utype.isTownHall)
  }
  
  def _assignWorker(worker:FriendlyUnitInfo) {
    if (_minerals.isEmpty) { return }
    _sortMinerals()
    val mineral = _minerals.head
    _workersByMineral(mineral).add(worker)
    _mineralByWorker.put(worker, mineral)
  }
  
  def _sortMinerals() {
    _minerals
      .sortBy(mineral => -mineral.mineralsLeft)
      .sortBy(mineral => _getTownHalls.map(_.position.getDistance(mineral.position)).min)
      .sortBy(mineral => _workersByMineral(mineral).size)
  }
  
  def _orderWorker(worker:FriendlyUnitInfo) {
    if (worker.isCarryingMinerals || worker.isCarryingGas) {
      //Can't spam return cargo
      if (worker.command.getUnitCommandType != UnitCommandType.Return_Cargo || ! worker.isMoving) {
        worker.baseUnit.returnCargo()
      }
    } else {
      val minerals = _mineralByWorker.get(worker)
      if (minerals.nonEmpty) {
        worker.baseUnit.gather(minerals.minBy(_.position.getDistance(worker.position)).baseUnit)
      }
    }
  }
}
