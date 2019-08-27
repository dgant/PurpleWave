package Planning.Plans.Macro.Protoss

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.{Get, GetAnother}
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

class BuildTowersAtBases(
  towersRequired  : Int,
  placementPylon  : PlacementProfile = PlacementProfiles.hugWorkersWithPylon,
  placementTower  : PlacementProfile = PlacementProfiles.hugWorkersWithCannon,
  towerClass      : UnitClass = Protoss.PhotonCannon)
    extends Plan {

  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray

    if (zones.nonEmpty) {
      if (towerClass == Protoss.PhotonCannon && With.units.existsOurs(Protoss.Forge)) {
        val towersRequired = zones.map(towerZone).sum
        With.scheduler.request(this, GetAnother(towersRequired, towerClass))
      }
      else {
        With.scheduler.request(this, Get(Protoss.Forge))
      }
    }
  }

  private val pylonBlueprintByZone = With.geography.zones
    .map(zone =>(
      zone,
      new Blueprint(this,
        building          = Some(Protoss.Pylon),
        requireZone       = Some(zone),
        requireCandidates = Some(zone.tilesSeq),
        placement         = Some(placementPylon))))
    .toMap

  private val towerBlueprintsByZone = With.geography.zones
    .map(zone => (
      zone,
      (1 to towersRequired).map(i =>
        new Blueprint(this,
          building          = Some(towerClass),
          requireZone       = Some(zone),
          requireCandidates = Some(zone.tilesSeq),
          placement         = Some(placementTower)))))
    .toMap

  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
  }

  private def towerZone(zone: Zone): Int = {
    lazy val pylonsInZone = zone.units.filter(_.is(Protoss.Pylon))
    lazy val towersInZone = zone.units.filter(_.is(towerClass))
    lazy val towersToAdd = towersRequired - towersInZone.size
    
    if (towersToAdd <= 0) {
      return 0
    }
    
    if (pylonsInZone.isEmpty) {
      With.groundskeeper.propose(pylonBlueprintByZone(zone))
      With.scheduler.request(this, GetAnother(1, Protoss.Pylon))
    }
    else if (pylonsInZone.exists(_.aliveAndComplete)) {
      // Defensive programming measure. If we try re-proposing fulfilled blueprints we may just build towers forever.
      val newBlueprints = towerBlueprintsByZone(zone).filterNot(With.groundskeeper.proposalsFulfilled.contains).take(towersToAdd)
      newBlueprints.foreach(With.groundskeeper.propose)
      return newBlueprints.size
    }
  
    0
  }
}
