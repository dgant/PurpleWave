package Planning.Plans.Macro.Expanding

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Plan
import ProxyBwapi.Races.Protoss

class BuildCannonsAtBases(count: Int) extends Plan {
  
  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet
    
    if (zones.nonEmpty) {
      if (With.units.ours.exists(_.is(Protoss.Forge))) {
        zones.foreach(cannonZone)
      }
      else {
        With.scheduler.request(this, RequestAtLeast(1, Protoss.Forge))
      }
    }
  }
  
  private val pylonBlueprintByZone = With.geography.zones
    .map(zone =>(
      zone,
      new Blueprint(this,
        building          = Some(Protoss.Pylon),
        requireZone       = Some(zone),
        placementProfile  = Some(PlacementProfiles.hugWorkersWithPylon))))
    .toMap
  
  private val cannonBlueprintsByZone = With.geography.zones
    .map(zone => (
      zone,
      (1 to count).map(i =>
        new Blueprint(this,
          building          = Some(Protoss.PhotonCannon),
          requireZone       = Some(zone),
          placementProfile  = Some(PlacementProfiles.hugWorkersWithCannon)))))
    .toMap
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
  }
  
  private def cannonZone(zone: Zone) {
    val cannonsInZone  = With.units.ours.count(unit => unit.is(Protoss.PhotonCannon) && unit.pixelCenter.zone == zone)
    val cannonsToAdd   = count - cannonsInZone
    
    if (cannonsToAdd <= 0) return
    
    With.groundskeeper.propose(pylonBlueprintByZone(zone))
    cannonBlueprintsByZone(zone).foreach(With.groundskeeper.propose)
    With.scheduler.request(this, RequestAnother(cannonsToAdd, Protoss.PhotonCannon))
  }
}
