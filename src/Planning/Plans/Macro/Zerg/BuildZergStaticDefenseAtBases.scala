package Planning.Plans.Macro.Zerg

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.GetAnother
import Planning.Plan
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass

class BuildZergStaticDefenseAtBases(
  towerClass: UnitClass,
  towersRequired: Int,
  placement: PlacementProfile = PlacementProfiles.defensive)
  extends Plan {
  
  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray
    
    if (zones.nonEmpty) {
      val creepColoniesRequired = zones.map(placeBuildingsInZone).sum
      With.scheduler.request(this, GetAnother(creepColoniesRequired, Zerg.CreepColony))
    }
  }
  
  private val blueprintsByZone = With.geography.zones
    .map(zone => (
      zone,
      (1 to towersRequired).map(i =>
        new Blueprint(
          building          = Some(Zerg.CreepColony),
          requireZone       = Some(zone),
          requireCandidates = Some(zone.tilesSeq),
          placement         = Some(placement)))))
    .toMap
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
  }
  
  private def placeBuildingsInZone(zone: Zone): Int = {
    lazy val towersInZone = With.units.countOursP(unit => Vector(Zerg.CreepColony, towerClass).contains(unit.unitClass) && unit.zone == zone)
    lazy val creepColoniesToAdd = towersRequired - towersInZone
    
    if (creepColoniesToAdd <= 0) {
      return 0
    }

    // Defensive programming measure. If we try re-proposing fulfilled blueprints we may just build cannons forever.
    val newBlueprints = blueprintsByZone(zone).take(creepColoniesToAdd)
    newBlueprints.foreach(With.groundskeeper.suggest)
    newBlueprints.size
  }
}
