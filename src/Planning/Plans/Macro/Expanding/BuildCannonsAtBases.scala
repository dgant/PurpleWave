package Planning.Plans.Macro.Expanding

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Composition.Property
import Planning.Plan
import ProxyBwapi.Races.Protoss

class BuildCannonsAtBases(initialCount: Int) extends Plan {
  
  val count: Property[Int] = new Property(initialCount)
  
  override def onUpdate() {
    val bases = eligibleBases
    if (eligibleBases.nonEmpty) {
      if (With.units.ours.exists(_.is(Protoss.PhotonCannon))) {
        eligibleBases.foreach(cannonBase)
      }
      else {
        With.scheduler.request(this, RequestAtLeast(1, Protoss.Forge))
      }
    }
  }
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases ++ With.geography.bases.filter(_.planningToTake)
  }
  
  private def cannonBase(base: Base) {
    val zone = base.zone
    val cannonsInExpansion = With.units.ours.filter(unit => unit.is(Protoss.PhotonCannon) && unit.pixelCenter.zone == zone)
    val pylonBlueprint = new Blueprint(this, argPlacement = Some(PlacementProfiles.mineralCannon), building = Some(Protoss.Pylon), zone = Some(base.zone))
    With.groundskeeper.propose(pylonBlueprint)
    for (i <- 0 to count.get) {
      val cannonBlueprint = new Blueprint(this, argPlacement = Some(PlacementProfiles.mineralCannon), building = Some(Protoss.PhotonCannon), zone = Some(base.zone))
      With.groundskeeper.propose(cannonBlueprint)
    }
    With.scheduler.request(this, RequestAnother(count.get - cannonsInExpansion.size, Protoss.PhotonCannon))
  }
}
