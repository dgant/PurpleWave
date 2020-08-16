package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plans.Placement.ProposePlacement
import ProxyBwapi.Races.Protoss

class TwoGatewaysAtNatural extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(Protoss.Pylon,   preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(Protoss.Gateway, preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(Protoss.Gateway, preferZone = Some(With.geography.ourNatural.zone)))
}

