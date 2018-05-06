package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

class BuildGasPumps(quantity: Int = Int.MaxValue, pumpType: UnitClass = With.self.gasClass) extends Plan {
  
  private lazy val blueprints = With.units.all.filter(_.unitClass.isGas).map(
    gas =>
    (
      gas.tileTopLeft,
      new Blueprint(
        this,
        building = Some(pumpType),
        requireZone = Some(gas.zone),
        requireCandidates = Some(Seq(gas.tileTopLeft))
      )
    ))
    .toMap
  
  override def onUpdate(): Unit = {
    val eligibleBases       = With.geography.ourBases.filter(base => base.townHall.exists(_.remainingCompletionFrames <= pumpType.buildFrames)).toSeq.sortBy(-_.gasLeft).sortBy(_.townHall.exists(_.complete))
    val eligibleGas         = eligibleBases.flatMap(_.gas)
    val eligibleGasToTake   = eligibleGas.filter(_.player.isNeutral)
    val eligibleBlueprints  = eligibleGasToTake.map(_.tileTopLeft).flatMap(blueprints.get)
    val finalBlueprints     = eligibleBlueprints.take(quantity)
    val gasToRequest        = Math.min(quantity, eligibleGas.size)
    finalBlueprints.foreach(With.groundskeeper.propose)
    With.scheduler.request(this, RequestAtLeast(gasToRequest, pumpType))
  }
  
}
