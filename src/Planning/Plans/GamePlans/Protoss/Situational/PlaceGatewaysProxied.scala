package Planning.Plans.GamePlans.Protoss.Situational

import Information.Geography.Types.Zone
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class PlaceGatewaysProxied(gatewayCount: Int, proxyZone: () => Option[Zone], allowBlockingBase: Boolean = true) extends ProposePlacement {
  
  private lazy val pylon =
    new Blueprint(
      building = Some(Protoss.Pylon),
      preferZone = proxyZone(),
      respectHarvesting = Some(!allowBlockingBase),
      placement = Some(PlacementProfiles.proxyPylon))

  private lazy val gateways = (0 to gatewayCount).map(unused =>
    new Blueprint(
      building = Some(Protoss.Gateway),
      preferZone = proxyZone(),
      respectHarvesting = Some(!allowBlockingBase),
      placement = Some(PlacementProfiles.proxyBuilding)))
  
  override lazy val blueprints: Vector[Blueprint] = Vector(pylon) ++ gateways
}
