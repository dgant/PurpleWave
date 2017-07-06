package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestUnitAtLeast, RequestUpgradeNext}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{ConsiderAttacking, ControlMap, DefendChokes}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientPylons, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class IslandCarriers extends Parallel {
  
  private class ExpandOverIsland extends RequireMiningBases {
    description.set("Fill island with expansions")
    basesDesired.set(With.geography.bases.count(base => With.paths.exists(base.heart, With.self.startTile)))
  }
  
  private class TechToCarriers extends Build(
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUnitAtLeast(1, Protoss.Stargate),
    RequestUnitAtLeast(1, Protoss.FleetBeacon),
    RequestUpgradeNext(Protoss.CarrierCapacity)
  )
  
  private class TechToArbiters extends Build(
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUnitAtLeast(1, Protoss.CitadelOfAdun),
    RequestUnitAtLeast(1, Protoss.Stargate),
    RequestUnitAtLeast(1, Protoss.TemplarArchives),
    RequestUnitAtLeast(1, Protoss.ArbiterTribunal)
  )
  
  private class TechToObservers extends Build(
    RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    RequestUnitAtLeast(1, Protoss.Observatory)
  )
  
  private class SpamUpgrades extends Build(
    RequestUnitAtLeast(2, Protoss.CyberneticsCore),
    RequestUpgradeNext(Protoss.AirDamage),
    RequestUpgradeNext(Protoss.AirArmor)
  )
  
  children.set(Vector(
    new RequireMiningBases(1),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new ExpandOverIsland,
    new BuildAssimilators,
    new TechToCarriers,
    new If(
      new UnitsAtLeast(4, UnitMatchType(Protoss.Carrier)),
      new Parallel(
        new SpamUpgrades,
        new TechToObservers,
        new TechToArbiters,
        new TrainContinuously(Protoss.Observer, 2),
        new TrainContinuously(Protoss.Arbiter, 2),
        new TrainContinuously(Protoss.Carrier)
      ),
      new Parallel(
        new TrainContinuously(Protoss.Scout, 3),
        new TrainContinuously(Protoss.Carrier),
        new TrainContinuously(Protoss.Observer, 1)
      )
    ),
    new OnMiningBases(1, new Build(RequestUnitAtLeast(3, Protoss.Stargate))),
    new OnMiningBases(2, new Build(RequestUnitAtLeast(5, Protoss.Stargate))),
    new OnMiningBases(3, new Build(RequestUnitAtLeast(8, Protoss.Stargate))),
    new ControlMap,
    new If(
      new UnitsAtLeast(6 * 8, UnitMatchType(Protoss.Interceptor)),
      new ConsiderAttacking,
      new DefendChokes),
    new FollowBuildOrder,
    new Gather
  ))
}
