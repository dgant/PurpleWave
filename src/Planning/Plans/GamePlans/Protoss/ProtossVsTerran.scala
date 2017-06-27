package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, RequestUpgradeLevel, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{Attack, ControlMap}
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtMost, HaveUpgrade, UnitsAtLeast}
import Planning.Plans.Macro.Reaction.{EnemyBio, EnemyBioAllIn}
import ProxyBwapi.Races.{Protoss, Terran}

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private val lateGameMassGateway = Vector[BuildRequest] (
    RequestUnitAtLeast(3,   Protoss.Nexus),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUpgradeLevel(    Protoss.ZealotSpeed,    1),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(1,   Protoss.Forge),
    RequestUpgradeLevel(    Protoss.GroundDamage,   1),
    RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    RequestUpgradeLevel(    Protoss.GroundDamage,   2),
    RequestUpgradeLevel(    Protoss.GroundDamage,   3)
  )
  
  private val lateGameCarriers = Vector[BuildRequest] (
    RequestUnitAtLeast(3,   Protoss.Nexus),
    RequestUnitAtLeast(1,   Protoss.Stargate),
    RequestUnitAtLeast(3,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.FleetBeacon),
    RequestUpgradeLevel(    Protoss.AirDamage,        1),
    RequestUnitAtLeast(3,   Protoss.Stargate),
    RequestUpgradeLevel(    Protoss.CarrierCapacity,  1),
    RequestUpgradeLevel(    Protoss.AirDamage,        2),
    RequestUpgradeLevel(    Protoss.AirDamage,        3),
    RequestUnitAtLeast(4,   Protoss.Nexus)
  )
  
  private class RespondToBioAllInWithReavers extends IfThenElse(
    new EnemyBioAllIn,
    new Build(ProtossBuilds.TechReavers)
  )
  
  private class RespondToBioWithReavers extends IfThenElse(
    new EnemyBio,
    new Build(ProtossBuilds.TechReavers)
  )
  
  private class TakeNatural extends IfThenElse(
    new Or(
      new UnitsAtLeast(3, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(2)
  )
  
  private class TakeThirdBase extends IfThenElse(
    new Or(
      new UnitsAtLeast(5, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(3)
  )
  
  private class TakeFourthBase extends IfThenElse(
    new Or(
      new UnitsAtLeast(5, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver)),
      new UnitsAtLeast(6, UnitMatchType(Protoss.Carrier))
    ),
    new RequireMiningBases(4)
  )
  
  private class BuildDragoonsUntilWeHaveZealotSpeed extends IfThenElse(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed),
      new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
  
  private class UpgradeCarriers extends IfThenElse(
    new UnitsAtLeast(1, UnitMatchType(Protoss.FleetBeacon)),
    new Build(RequestUpgradeLevel(Protoss.CarrierCapacity))
  )
  
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
    new Build(ProtossBuilds.OpeningTwoGate1015Dragoons),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    new RespondToBioAllInWithReavers,
    new MatchMiningBases(1),
    new TakeNatural,
    new RespondToBioWithReavers,
    new TakeThirdBase,
    new TakeFourthBase,
    new TrainContinuously(Protoss.Reaver, 2),
    new UpgradeCarriers,
    new TrainContinuously(Protoss.Carrier),
    new BuildDragoonsUntilWeHaveZealotSpeed,
    new Build(lateGameCarriers),
    new ScoutAt(10),
    new ControlMap,
    new TimingAttacks
  ))
}
