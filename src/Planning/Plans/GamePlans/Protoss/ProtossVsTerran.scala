package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, RequestUpgradeLevel, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{Attack, ControlMap}
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtMost, UnitsAtLeast}
import ProxyBwapi.Races.{Protoss, Terran}

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private val lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(5,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.Stargate),
    RequestUnitAtLeast(1,   Protoss.FleetBeacon),
    RequestUpgradeLevel(    Protoss.AirDamage,        1),
    RequestUnitAtLeast(3,   Protoss.Stargate),
    RequestUpgradeLevel(    Protoss.AirDamage,        2),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUpgradeLevel(    Protoss.ZealotSpeed,      1),
    RequestUpgradeLevel(    Protoss.AirDamage,        3),
    RequestUnitAtLeast(9,   Protoss.Gateway)
  )
  
  private class TakeNatural extends IfThenElse(
    new Or(
      new UnitsAtLeast(3, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(2)
  )
  
  private class ConsiderTakingFourthBase extends IfThenElse(
    new Or(
      new UnitsAtLeast(20, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new UnitsAtLeast(6, UnitMatchType(Protoss.Carrier))
    ),
    new RequireMiningBases(4)
  )
  
  private class UpgradeCarrierCapacity extends IfThenElse(
    new UnitsAtLeast(1, UnitMatchType(Protoss.FleetBeacon)),
    new Build(RequestUpgradeLevel(Protoss.CarrierCapacity, 1)))
  
  private class TimingAttacks extends IfThenElse(
    new Or(
      new And(
        new EnemyUnitsAtMost(0, UnitMatchType(Terran.SiegeTankSieged)),
        new EnemyUnitsAtMost(0, UnitMatchType(Terran.SiegeTankUnsieged))),
      new UnitsAtLeast(6 * 6, UnitMatchType(Protoss.Interceptor))
    ),
    new Attack)
  
  children.set(Vector(
    new RequireMiningBases(1),
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateway_EarlyThird),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new ProtossVsTerranIdeas.RespondToBioAllInWithReavers,
    new MatchMiningBases(1),
    new ProtossVsTerranIdeas.RespondToBioWithReavers,
    new ConsiderTakingFourthBase,
    new TrainContinuously(Protoss.Reaver, 2),
    new UpgradeCarrierCapacity,
    new TrainContinuously(Protoss.Carrier),
    new ProtossVsTerranIdeas.BuildDragoonsUntilWeHaveZealotSpeed,
    new Build(RequestUpgradeLevel(Protoss.DragoonRange, 1)),
    new RequireMiningBases(3),
    new BuildAssimilators,
    new Build(lateGame),
    new ScoutAt(14),
    new ControlMap,
    new TimingAttacks
  ))
}
