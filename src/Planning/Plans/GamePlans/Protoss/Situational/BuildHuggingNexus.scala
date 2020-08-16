package Planning.Plans.GamePlans.Protoss.Situational

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Placement.ProposePlacement
import ProxyBwapi.Races.Protoss

class BuildHuggingNexus extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(Protoss.Pylon,            placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Gateway,          placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Gateway,          placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Pylon,            placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.CyberneticsCore,  placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Pylon,            placement = Some(PlacementProfiles.hugTownHall)))
}

