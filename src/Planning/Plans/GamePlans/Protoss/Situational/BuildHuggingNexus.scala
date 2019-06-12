package Planning.Plans.GamePlans.Protoss.Situational

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement

class BuildHuggingNexus extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(placement = Some(PlacementProfiles.hugTownHall)))
}

