package Planning.Plans.Protoss.Situational

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class TwoGatewaysAtNexus extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(this, argMargin = Some(false),  building = Some(Protoss.Pylon),    argPlacement = Some(PlacementProfiles.hugTheNexus)),
    new Blueprint(this, argMargin = Some(false),  building = Some(Protoss.Gateway),  argPlacement = Some(PlacementProfiles.hugTheNexus)),
    new Blueprint(this,                           building = Some(Protoss.Gateway),  argPlacement = Some(PlacementProfiles.hugTheNexus)),
    new Blueprint(this,                           building = Some(Protoss.Pylon),    argPlacement = Some(PlacementProfiles.hugTheNexus)),
    new Blueprint(this,                           building = Some(Protoss.Pylon),    argPlacement = Some(PlacementProfiles.hugTheNexus)))
}

