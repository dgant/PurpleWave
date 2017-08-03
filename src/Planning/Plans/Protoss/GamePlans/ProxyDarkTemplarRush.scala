package Planning.Plans.Protoss.GamePlans

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{AddSupplyWhenSupplyBlocked, Gather, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Scouting.ScoutAt
import Planning.ProxyPlanner
import ProxyBwapi.Races.Protoss

class ProxyDarkTemplarRush extends Parallel {
  
  children.set(Vector(
    new ProposePlacement {
      override lazy val blueprints: Iterable[Blueprint] = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon),   placementProfile = Some(PlacementProfiles.hugTownHall)),
        new Blueprint(this, building = Some(Protoss.Gateway), placementProfile = Some(PlacementProfiles.hugTownHall)),
        new Blueprint(this, building = Some(Protoss.Pylon),   placementProfile = Some(PlacementProfiles.proxyPylon),    preferZone = ProxyPlanner.proxyAutomaticSneaky),
        new Blueprint(this, building = Some(Protoss.Gateway), placementProfile = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky),
        new Blueprint(this, building = Some(Protoss.Gateway), placementProfile = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky))
    },
    
    // Might be the fastest possible DT rush.
    // An example: https://youtu.be/ca40eQ1s7iw
    new Build(
      RequestAtLeast(8, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(10, Protoss.Probe),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(11, Protoss.Probe),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(13, Protoss.Probe),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Zealot),
      RequestAtLeast(15, Protoss.Probe),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(2, Protoss.Pylon),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(2, Protoss.Gateway)),
    new AddSupplyWhenSupplyBlocked,
    new TrainContinuously(Protoss.DarkTemplar),
    new ScoutAt(11),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}
