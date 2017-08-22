package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

class BuildGasPumps(quantity: Int = Int.MaxValue, pumpType: UnitClass = With.self.gasClass) extends Plan {
  
  private lazy val blueprints = With.geography.bases.map(
    base =>
    (
      base,
      base.gas.map(gas =>
        new Blueprint(
          this,
          building = Some(pumpType),
          requireZone = Some(base.zone),
          requireCandidates = Some(Seq(gas.tileTopLeft))
      )))
    )
    .toMap
  
  override def onUpdate(): Unit = {
    val eligibleBases       = With.geography.ourBases.filter(base => base.townHall.exists(_.remainingBuildFrames <= pumpType.buildFrames)).toSeq.sortBy(-_.gasLeft).sortBy(_.townHall.exists(_.complete))
    val eligibleGas         = eligibleBases.flatMap(_.gas).filter(_.player.isNeutral)
    val eligibleBlueprints  = eligibleBases.flatMap(blueprints)
    val finalBlueprints     = eligibleBlueprints.take(quantity)
    finalBlueprints.foreach(With.groundskeeper.propose)
    With.scheduler.request(this, RequestAtLeast(eligibleBlueprints.size, pumpType))
  }
  
}
