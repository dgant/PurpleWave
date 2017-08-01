package Planning.Plans.Protoss.Situational

import Macro.Architecture.Blueprint
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class Nexus2GateThenCannons extends ProposePlacement {
  override lazy val blueprints: Vector[Blueprint] =
    Vector(
      new Blueprint(this, building = Some(Protoss.Pylon))) ++
        new ForgeFastExpand().blueprints
}

