package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class ForgeFastExpand(var cannonsInFront: Boolean) extends ProposePlacement {
  override lazy val blueprints: Iterable[Blueprint] = Vector(
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannonPylon), building = Some(Protoss.Pylon),         preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(if (cannonsInFront) 5.0 else 0.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannonPylon), building = Some(Protoss.Forge),         preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(if (cannonsInFront) 5.0 else 0.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(2.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(2.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(2.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(2.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(3.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(3.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(3.0 * 32.0)),
    new Blueprint(this, preferWallPlacement = Some(true),                           placementProfile = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  preferZone = With.geography.ourNatural.map(_.zone), preferredDistanceFromExit = Some(3.0 * 32.0)),
    new Blueprint(this, building = Some(Protoss.Pylon),                 preferZone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.Pylon),                 preferZone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.Gateway),               preferZone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.CyberneticsCore),       preferZone = Some(With.geography.ourMain.zone))
  )
}

