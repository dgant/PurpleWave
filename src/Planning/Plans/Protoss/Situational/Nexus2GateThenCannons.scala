package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class Nexus2GateThenCannons extends ProposePlacement {
  override lazy val blueprints: Vector[Blueprint] =
    Vector(
      new Blueprint(this, building = Some(Protoss.Pylon)),
      new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(With.geography.ourNatural.zone),  placement = Some(PlacementProfiles.wallPylon)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(With.geography.ourNatural.zone),  placement = Some(PlacementProfiles.wallCannon)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(With.geography.ourNatural.zone),  placement = Some(PlacementProfiles.wallCannon)))
}

