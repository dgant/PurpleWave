package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, RequestUpgradeLevel}
import Planning.Plans.Army.{ConsiderAttacking, ControlMap}
import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildAssimilators, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, OnMiningBases}
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private class OnThreeBases_SpeedlotsAndObservers extends OnMiningBases(3,
    new Build(
      RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
      RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
      RequestUnitAtLeast(1,   Protoss.Observatory),
      RequestUpgradeLevel(    Protoss.ZealotSpeed,      1),
      RequestUnitAtLeast(4,   Protoss.Gateway),
      RequestUnitAtLeast(8,   Protoss.Gateway),
      RequestUnitAtLeast(10,  Protoss.Gateway),
      RequestUnitAtLeast(14,  Protoss.Gateway)
    ))
  
  private class OnThreeGas_Arbiters extends OnGasBases(3,
    new Build(
      RequestUnitAtLeast(1,   Protoss.TemplarArchives),
      RequestUnitAtLeast(1,   Protoss.Stargate),
      RequestUnitAtLeast(1,   Protoss.ArbiterTribunal)
    ))
  
  private class OnThreeBases_WeaponsUpgrades extends OnMiningBases(3,
    new Build(
      RequestUnitAtLeast(1,   Protoss.Forge),
      RequestUpgradeLevel(    Protoss.GroundDamage,     1)
    ))
  children.set(Vector(
    new RequireMiningBases(1),
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateway_EarlyThird: _*),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new ProtossVsTerranIdeas.RespondToBioAllInWithReavers,
    new MatchMiningBases(1),
    new RequireMiningBases(2),
    new ProtossVsTerranIdeas.RespondToBioWithReavers,
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Observer, 3),
    new TrainContinuously(Protoss.Arbiter, 3),
    new ProtossVsTerranIdeas.BuildDragoonsUntilWeHaveZealotSpeed,
    new Build(RequestUpgradeLevel(Protoss.DragoonRange, 1)),
    new Build(RequestUnitAtLeast(2, Protoss.Gateway)),
    new RequireMiningBases(3),
    new BuildAssimilators,
    new OnThreeBases_SpeedlotsAndObservers,
    new OnThreeGas_Arbiters,
    new OnThreeBases_WeaponsUpgrades,
    new OnMiningBases(4, new Build(RequestUnitAtLeast(14, Protoss.Gateway))),
    new RequireMiningBases(4),
    new ScoutAt(14),
    new ScoutExpansionsAt(100),
    new ControlMap,
    new ConsiderAttacking
  ))
}
