package Planning.Plans.Macro.Expanding

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Plan
import ProxyBwapi.Races.Protoss

class BuildCannonsAtBases(cannonsRequired: Int) extends Plan {
  
  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray
    
    if (zones.nonEmpty) {
      if (With.units.ours.exists(_.is(Protoss.Forge))) {
        val cannonsRequired = zones.map(cannonZone).sum
        With.scheduler.request(this, RequestAnother(cannonsRequired, Protoss.PhotonCannon))
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
        requireCandidates = Some(zone.tiles),
        placementProfile  = Some(PlacementProfiles.hugWorkersWithPylon))))
    .toMap
  
  private val cannonBlueprintsByZone = With.geography.zones
    .map(zone => (
      zone,
      (1 to cannonsRequired).map(i =>
        new Blueprint(this,
          building          = Some(Protoss.PhotonCannon),
          requireZone       = Some(zone),
          requireCandidates = Some(zone.tiles),
          placementProfile  = Some(PlacementProfiles.hugWorkersWithCannon)))))
    .toMap
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases.toSeq.sortBy(_.heart.i) //Arbitrary but stable ordering
  }
  
  private def cannonZone(zone: Zone): Int = {
    lazy val pylonsInZone    = With.units.ours.filter(unit => unit.is(Protoss.Pylon)        && unit.pixelCenter.zone == zone)
    lazy val cannonsInZone   = With.units.ours.filter(unit => unit.is(Protoss.PhotonCannon) && unit.pixelCenter.zone == zone)
    lazy val cannonsToAdd    = cannonsRequired - cannonsInZone.size
    
    if (cannonsToAdd <= 0) {
      return 0
    }
    
    if (pylonsInZone.isEmpty) {
      With.groundskeeper.propose(pylonBlueprintByZone(zone))
      With.scheduler.request(this, RequestAnother(1, Protoss.Pylon))
    }
    else if (pylonsInZone.exists(_.aliveAndComplete)) {
      // Defensive programming measure. If we try re-proposing fulfilled blueprints we may just build cannons forever.
      val newBlueprints = cannonBlueprintsByZone(zone).filterNot(With.groundskeeper.proposalsFulfilled.contains).take(cannonsToAdd)
      newBlueprints.foreach(With.groundskeeper.propose)
      return newBlueprints.size
    }
  
    0
  }
}
