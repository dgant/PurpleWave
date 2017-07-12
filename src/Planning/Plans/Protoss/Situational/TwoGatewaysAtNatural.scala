package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class TwoGatewaysAtNatural extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(this, argMargin = Some(false),  building = Some(Protoss.Pylon),    zone = With.geography.ourNatural.map(_.zone)),
    new Blueprint(this, argMargin = Some(false),  building = Some(Protoss.Gateway),  zone = With.geography.ourNatural.map(_.zone)),
    new Blueprint(this,                           building = Some(Protoss.Gateway),  zone = With.geography.ourNatural.map(_.zone)))
}

