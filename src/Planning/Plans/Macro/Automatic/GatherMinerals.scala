package Planning.Plans.Macro.Automatic

import Micro.Intentions.Intention
import Performance.Caching.Limiter
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Startup.With

import scala.collection.mutable

class GatherMinerals extends Plan {

  val miners = new LockUnits
  miners.unitMatcher.set(UnitMatchWorker)
  miners.unitCounter.set(UnitCountEverything)
  
  override def getChildren: Iterable[Plan] = List(miners)
  
  private var minerals:List[UnitInfo] = List.empty
  private val workersByMineral = new mutable.HashMap[UnitInfo, mutable.HashSet[FriendlyUnitInfo]] {
    override def default(key: UnitInfo): mutable.HashSet[FriendlyUnitInfo] = { put(key, mutable.HashSet.empty); this(key)}}
  private val mineralByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  private val lastOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] {
    override def default(key: FriendlyUnitInfo): Int = { put(key, Int.MinValue); this(key) }}
  
  override def onFrame() {
    miners.onFrame()
    resetAssignmentsLimiter.act()
    assignWorkers()
    miners.units.foreach(orderWorker)
  }
  
  val resetAssignmentsLimiter = new Limiter(4, resetAssignments)
  private def resetAssignments() {
    val allMinerals = With.units.neutral.filter(_.isMinerals)
    val completeBases = With.geography.ourBases.filter(_.townHall.exists(_.complete))
    mineralByWorker.clear()
    workersByMineral.clear()
    minerals = allMinerals.filter(mineral => completeBases.exists(_.harvestingArea.contains(mineral.tileCenter))).toList
    
    //Long-distance mining
    if (minerals.isEmpty) {
      minerals = allMinerals.toList.sortBy(_.distance(With.geography.home))
    }
  }
  
  private def assignWorkers() {
    val unassignedWorkers = new mutable.HashSet[FriendlyUnitInfo]
    unassignedWorkers ++= miners.units.diff(mineralByWorker.keySet)
    sortMinerals()
    if (minerals.isEmpty) { return }
    while (unassignedWorkers.nonEmpty) {
      minerals.foreach(mineral => {
        if (unassignedWorkers.nonEmpty) {
          val worker = unassignedWorkers.minBy(_.pixelCenter.getDistance(mineral.pixelCenter))
          workersByMineral(mineral).add(worker)
          mineralByWorker.put(worker, mineral)
          unassignedWorkers.remove(worker)
          orderWorker(worker)
        }
      })
    }
  }
  
  private def assignWorker(worker:FriendlyUnitInfo) {
    if (minerals.isEmpty) return
    sortMinerals()
    val mineral = minerals.head
    workersByMineral(mineral).add(worker)
    mineralByWorker.put(worker, mineral)
  }
  
  private def sortMinerals() {
    val townHalls = With.geography.bases.filter(_.townHall.exists(_.complete)).map(_.centerTile)
    if (townHalls.isEmpty) return
    minerals
      .sortBy(mineral => -mineral.mineralsLeft)
      .sortBy(mineral => townHalls.map(_.getDistance(mineral.tileCenter)).min)
      .sortBy(mineral => workersByMineral(mineral).size)
  }
  
  private def orderWorker(worker:FriendlyUnitInfo) {
    if (worker.isGatheringMinerals) {
      mineralByWorker.get(worker).foreach(mineral => {
        if (mineral.pixelCenter.getDistance(worker.pixelCenter) > 32 * 12) {
          gather(worker, mineral)
        }
      })
    } else {
      mineralByWorker.get(worker).foreach(mineral => gather(worker, mineral))
    }
  }
  
  private def gather(worker:FriendlyUnitInfo, mineral:UnitInfo) = {
    With.executor.intend(new Intention(this, worker) { toGather = Some(mineral) })
  }
}
