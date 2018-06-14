package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{Get, Upgrade}
import Planning.Composition.UnitMatchers.UnitMatchMobileDetectors
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import Planning.ProxyPlanner
import ProxyBwapi.Races.Protoss

class ProxyDarkTemplarRush extends Parallel {
  
  children.set(Vector(
    
    new RequireEssentials,
    
    new ProposePlacement {
      override lazy val blueprints: Seq[Blueprint] = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon)),
        new Blueprint(this, building = Some(Protoss.Gateway)),
        new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.proxyPylon),    preferZone = ProxyPlanner.proxyAutomaticSneaky),
        new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky),
        new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky))
    },
    
    // Might be the fastest possible DT rush.
    // An example: https://youtu.be/ca40eQ1s7iw
    
    new If(
      new UnitsAtLeast(1, Protoss.TemplarArchives, complete = false),
      new If(
        new EnemiesAtLeast(1, UnitMatchMobileDetectors),
        new Parallel(
          new RequireMiningBases(2),
          new Build(
            Upgrade(Protoss.DragoonRange),
            Get(2, Protoss.DarkTemplar))),
        new TrainContinuously(Protoss.DarkTemplar))),
    
    new Build(
      Get(1, Protoss.Nexus),
      Get(8, Protoss.Probe),
      Get(1, Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(1, Protoss.Gateway),
      Get(11, Protoss.Probe),
      Get(1, Protoss.Assimilator),
      Get(13, Protoss.Probe),
      Get(1, Protoss.CyberneticsCore),
      Get(1, Protoss.Zealot),
      Get(1, Protoss.CitadelOfAdun),
      Get(2, Protoss.Zealot),
      Get(2, Protoss.Pylon),
      Get(1, Protoss.TemplarArchives),
      Get(3, Protoss.Gateway),
      Get(15, Protoss.Probe),
      Get(4, Protoss.Gateway)),
    
    new RequireSufficientSupply,
    new TrainContinuously(Protoss.Dragoon),
    new TrainWorkersContinuously,
    new Build(Get(4, Protoss.Gateway)),
    new RequireMiningBases(2),
    new BuildGasPumps,
    new Build(Get(8, Protoss.Gateway)),
    
    new If(new Not(new FoundEnemyBase), new ScoutAt(11)),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}
