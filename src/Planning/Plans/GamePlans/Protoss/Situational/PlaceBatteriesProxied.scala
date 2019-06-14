package Planning.Plans.GamePlans.Protoss.Situational

import Information.Geography.Types.Zone
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class PlaceBatteriesProxied(batteryCount: Int, proxyZone: () => Option[Zone], allowBlockingBase: Boolean = true) extends ProposePlacement {

  private lazy val pylon =
    new Blueprint(
      Protoss.Pylon,
      requireZone = proxyZone(),
      respectHarvesting = Some(!allowBlockingBase),
      placement = Some(PlacementProfiles.proxyPylon))

  private lazy val batteries = (0 to batteryCount).map(unused =>
    new Blueprint(
      Protoss.ShieldBattery,
      requireZone = proxyZone(),
      respectHarvesting = Some(!allowBlockingBase),
      placement = Some(PlacementProfiles.proxyTowardsEnemy)))
  
  override lazy val blueprints: Vector[Blueprint] = Vector(pylon) ++ batteries
}
