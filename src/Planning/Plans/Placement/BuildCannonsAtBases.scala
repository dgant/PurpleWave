package Planning.Plans.Placement

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.{Get, GetAnother}
import Planning.Plan
import ProxyBwapi.Races.Protoss

class BuildCannonsAtBases(
  cannonsRequired  : Int,
  placementPylon   : PlacementProfile = PlacementProfiles.hugWorkersWithPylon,
  placementCannon  : PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends Plan {
  
  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray
    
    if (zones.nonEmpty) {
      if (With.units.existsOurs(Protoss.Forge)) {
        val cannonsRequired = zones.map(cannonZone).sum
        With.scheduler.request(this, GetAnother(cannonsRequired, Protoss.PhotonCannon))
      }
      else {
        With.scheduler.request(this, Get(Protoss.Forge))
      }
    }
  }
  
  private val pylonBlueprintByZone = With.geography.zones
    .map(zone =>(
      zone,
      new Blueprint(
        Protoss.Pylon,
        requireZone       = Some(zone),
        requireCandidates = Some(zone.tilesSeq),
        placement         = Some(placementPylon))))
    .toMap
  
  private val cannonBlueprintsByZone = With.geography.zones
    .map(zone => (
      zone,
      (1 to cannonsRequired).map(i =>
        new Blueprint(
          Protoss.PhotonCannon,
          requireZone       = Some(zone),
          requireCandidates = Some(zone.tilesSeq),
          placement         = Some(placementCannon)))))
    .toMap
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
  }
  
  private def cannonZone(zone: Zone): Int = {
    lazy val pylonsInZone    = zone.units.filter(_.is(Protoss.Pylon))
    lazy val cannonsInZone   = zone.units.filter(_.is(Protoss.PhotonCannon))
    lazy val cannonsToAdd    = cannonsRequired - cannonsInZone.size
    
    if (cannonsToAdd <= 0) {
      return 0
    }
    
    if (pylonsInZone.isEmpty) {
      With.groundskeeper.suggest(pylonBlueprintByZone(zone))
      With.scheduler.request(this, GetAnother(1, Protoss.Pylon))
    }
    else if (pylonsInZone.exists(_.aliveAndComplete)) {
      val newBlueprints = cannonBlueprintsByZone(zone).take(cannonsToAdd)
      newBlueprints.foreach(With.groundskeeper.suggest)
      return newBlueprints.size
    }
  
    0
  }
}
