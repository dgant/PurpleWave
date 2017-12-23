package Planning.Plans.GamePlans.Protoss.Situational

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class BuildHuggingNexus extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),    placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Protoss.Pylon),    placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Protoss.Pylon),    placement = Some(PlacementProfiles.hugTownHall)))
}

