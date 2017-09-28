package Planning.Plans.Protoss.Situational

import Information.Geography.Types.Zone
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class PlaceGatewaysProxied(gateways: Int, proxyZone: () => Option[Zone]) extends ProposePlacement {
  
  private lazy val pylon    =                           new Blueprint(this, building = Some(Protoss.Pylon),   preferZone = proxyZone(), respectHarvesting = false, placement = Some(PlacementProfiles.proxyPylon))
  private lazy val gateway  = (0 to gateways).map(i =>  new Blueprint(this, building = Some(Protoss.Gateway), preferZone = proxyZone(), respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
  
  override lazy val blueprints: Vector[Blueprint] = Vector(pylon) ++ gateway
}
