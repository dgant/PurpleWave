package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class TwoGatewaysAtNatural extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(building = Some(Protoss.Pylon),   preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(building = Some(Protoss.Gateway), preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(building = Some(Protoss.Gateway), preferZone = Some(With.geography.ourNatural.zone)))
}

