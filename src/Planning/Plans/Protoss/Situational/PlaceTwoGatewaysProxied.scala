package Planning.Plans.Protoss.Situational

import Information.Geography.Types.Zone
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class PlaceTwoGatewaysProxied(proxyZone: () => Option[Zone]) extends ProposePlacement {
  
  override lazy val blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),   preferZone = proxyZone(), respectHarvesting = false, placement = Some(PlacementProfiles.proxyPylon)),
    new Blueprint(this, building = Some(Protoss.Gateway), preferZone = proxyZone(), respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)),
    new Blueprint(this, building = Some(Protoss.Gateway), preferZone = proxyZone(), respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
}
