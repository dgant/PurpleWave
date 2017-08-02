package Planning.Plans.Protoss.Situational

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class TwoGatewaysAtNexus extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(this, preferMargin = Some(false), building = Some(Protoss.Pylon),    placementProfile = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, preferMargin = Some(false), building = Some(Protoss.Gateway),  placementProfile = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this,                             building = Some(Protoss.Gateway),  placementProfile = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this,                             building = Some(Protoss.Pylon),    placementProfile = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this,                             building = Some(Protoss.Pylon),    placementProfile = Some(PlacementProfiles.hugTownHall)))
}

