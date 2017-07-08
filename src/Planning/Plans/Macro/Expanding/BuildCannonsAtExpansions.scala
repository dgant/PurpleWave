package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Composition.Property
import Planning.Plan
import ProxyBwapi.Races.Protoss

class BuildCannonsAtExpansions(initialCount: Int) extends Plan {
  
  val count: Property[Int] = new Property(initialCount)
  
  override def onUpdate() {
    
    With.scheduler.request(this, RequestAtLeast(1, Protoss.Forge))
    
    val expansions = (With.geography.ourBases ++ With.geography.bases.filter(_.planningToTake))
      .filterNot(With.geography.ourMain.contains)
      .filterNot(With.geography.ourNatural.contains)
      .toSet
    
    expansions.foreach(expansion => {
      val zone = expansion.zone
      val cannonsInExpansion = With.units.ours.filter(unit => unit.is(Protoss.PhotonCannon) && unit.pixelCenter.zone == zone)
      val pylonBlueprint = new Blueprint(this, argPlacement = Some(PlacementProfiles.mineralCannon), building = Some(Protoss.Pylon), zone = Some(expansion.zone))
      With.groundskeeper.propose(pylonBlueprint)
      for (i <- 0 to count.get) {
        val cannonBlueprint = new Blueprint(this, argPlacement = Some(PlacementProfiles.mineralCannon), building = Some(Protoss.PhotonCannon), zone = Some(expansion.zone))
        With.groundskeeper.propose(cannonBlueprint)
      }
      With.scheduler.request(this, RequestAnother(count.get - cannonsInExpansion.size, Protoss.PhotonCannon))
    })
  }
}
