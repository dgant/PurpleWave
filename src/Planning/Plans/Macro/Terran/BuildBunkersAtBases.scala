package Planning.Plans.Macro.Terran

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.GetAnother
import Planning.Plan
import ProxyBwapi.Races.Terran

class BuildBunkersAtBases(
  bunkersRequired: Int = 1,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends Plan {
  
  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray
    
    if (zones.nonEmpty) {
      val bunkersRequired = zones.map(placeBuildingsInZone).sum
      With.scheduler.request(this, GetAnother(bunkersRequired, Terran.Bunker))
    }
  }
  
  private val blueprintsByZone = With.geography.zones
    .map(zone => (
      zone,
      (1 to bunkersRequired).map(i =>
        new Blueprint(this,
          building          = Some(Terran.Bunker),
          requireZone       = Some(zone),
          requireCandidates = Some(zone.tilesSeq),
          placement         = Some(placement)))))
    .toMap
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
  }
  
  private def placeBuildingsInZone(zone: Zone): Int = {
    lazy val towersInZone = With.units.countOursP(u => u.is(Terran.Bunker) && u.zone == zone)
    lazy val bunkersToAdd = bunkersRequired - towersInZone
    
    if (bunkersToAdd <= 0) {
      return 0
    }
  
    // Defensive programming measure. If we try re-proposing fulfilled blueprints we may just build cannons forever.
    val newBlueprints = blueprintsByZone(zone).filterNot(With.groundskeeper.proposalsFulfilled.contains).take(bunkersToAdd)
    newBlueprints.foreach(With.groundskeeper.propose)
    newBlueprints.size
  }
}
