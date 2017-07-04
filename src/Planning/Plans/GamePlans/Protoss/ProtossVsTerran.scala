package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, RequestUpgradeLevel}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{ConsiderAttacking, ControlMap}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildAssimilators, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, OnMiningBases, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private class TakeThirdBase extends If(
    new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
    new RequireMiningBases(3)
  )
  
  private class OnThreeBases_Carriers extends OnMiningBases(3,
    new Build(
      RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
      RequestUnitAtLeast(1,   Protoss.Stargate),
      RequestUnitAtLeast(5,   Protoss.Gateway),
      RequestUpgradeLevel(    Protoss.ZealotSpeed),
      RequestUnitAtLeast(1,   Protoss.FleetBeacon),
      RequestUnitAtLeast(3,   Protoss.Stargate),
      RequestUnitAtLeast(1,   Protoss.Stargate),
      RequestUpgradeLevel(    Protoss.AirDamage,      1),
      RequestUpgradeLevel(    Protoss.CarrierCapacity),
      RequestUpgradeLevel(    Protoss.AirDamage,      2),
      RequestUpgradeLevel(    Protoss.AirDamage,      3)
    ))
  
  private class OnThreeBases_SpeedlotsAndObservers extends OnMiningBases(3,
    new Build(
      RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
      RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
      RequestUpgradeLevel(    Protoss.ZealotSpeed),
      RequestUnitAtLeast(1,   Protoss.Observatory)
    ))
  
  private class OnThreeGas_Arbiters extends OnGasBases(3,
    new Build(
      RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
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
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new ProtossVsTerranIdeas.RespondToBioAllInWithReavers,
    new MatchMiningBases(1),
    new RequireMiningBases(2),
    new ProtossVsTerranIdeas.RespondToBioWithReavers,
    new TakeThirdBase,
    new OnMiningBases(3, new BuildAssimilators),
    new OnThreeBases_SpeedlotsAndObservers,
    new OnThreeGas_Arbiters,
    new OnThreeBases_WeaponsUpgrades,
    new TrainContinuously(Protoss.Carrier),
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Arbiter, 3),
    new TrainContinuously(Protoss.Observer, 3),
    new ProtossVsTerranIdeas.BuildDragoonsUntilWeHaveZealotSpeed,
    new Build(RequestUpgradeLevel(Protoss.DragoonRange, 1)),
    new Build(RequestUnitAtLeast(2, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(RequestUnitAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(RequestUnitAtLeast(15, Protoss.Gateway)),
    new ScoutAt(14),
    new ScoutExpansionsAt(100),
    new ControlMap,
    new ConsiderAttacking
  ))
}
