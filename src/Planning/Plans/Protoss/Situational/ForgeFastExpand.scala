package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class ForgeFastExpand extends ProposePlacement {
  override lazy val blueprints: Iterable[Blueprint] = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannonPylon)),
    new Blueprint(this, building = Some(Protoss.Forge),           preferZone  = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannonPylon)),
    new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannon)),
    new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannon)),
    new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannon)),
    new Blueprint(this, building = Some(Protoss.Gateway),         preferZone  = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannon)),
    new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannon)),
    new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = With.geography.ourNatural.map(_.zone),  placementProfile = Some(PlacementProfiles.cannon)),
    new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.Pylon),           preferZone  = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.CyberneticsCore), preferZone  = Some(With.geography.ourMain.zone))
  )
}

