package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgradeNext}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{ConsiderAttacking, ControlMap, DefendHearts}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, BuildCannonsAtBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, OnMiningBases, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.FindExpansions
import ProxyBwapi.Races.{Protoss, Zerg}

class IslandCarriers extends Parallel {
  
  private class ExpandOverIsland extends RequireMiningBases {
    description.set("Fill island with expansions")
    basesDesired.set(
      if (With.strategy.isPlasma)
        3
      else
        With.geography.bases.count(_.zone.canWalkTo(With.geography.ourMain.zone)))
  }
  
  private class TechToCarriers extends Build(
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(1, Protoss.FleetBeacon),
    RequestUpgradeNext(Protoss.AirDamage),
    RequestUpgradeNext(Protoss.CarrierCapacity)
  )
  
  private class TechToArbiters extends Build(
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(1, Protoss.CitadelOfAdun),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(1, Protoss.TemplarArchives),
    RequestAtLeast(1, Protoss.ArbiterTribunal)
  )
  
  private class TechToObservers extends Build(
    RequestAtLeast(1, Protoss.RoboticsFacility),
    RequestAtLeast(1, Protoss.Observatory)
  )
  
  private class SpamUpgrades extends Build(
    RequestAtLeast(2, Protoss.CyberneticsCore),
    RequestUpgradeNext(Protoss.AirDamage),
    RequestUpgradeNext(Protoss.AirArmor)
  )
  
  children.set(Vector(
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    new ExpandOverIsland,
    new TechToCarriers,
    new TrainMatchingRatio(Protoss.Corsair, UnitMatchType(Zerg.Mutalisk), 1.5),
    new If(
      new UnitsAtLeast(4, UnitMatchType(Protoss.Carrier)),
      new Parallel(
        new OnMiningBases(2, new Parallel(new SpamUpgrades, new TechToObservers)),
        new OnMiningBases(3, new Parallel(new TechToArbiters)),
        new TrainContinuously(Protoss.Observer, 2),
        new TrainContinuously(Protoss.Arbiter, 2),
        new TrainContinuously(Protoss.Carrier)),
      new Parallel(
        new TrainContinuously(Protoss.Scout, 3),
        new TrainContinuously(Protoss.Carrier),
        new TrainContinuously(Protoss.Observer, 1))),
    new OnGasBases(1, new Build(RequestAtLeast(3, Protoss.Stargate))),
    new OnGasBases(2, new Build(RequestAtLeast(5, Protoss.Stargate))),
    new OnGasBases(3, new Build(RequestAtLeast(8, Protoss.Stargate))),
    new BuildCannonsAtBases(6),
    new FindExpansions { scouts.get.unitMatcher.set(UnitMatchType(Protoss.Scout)) },
    new ControlMap,
    new If(
      new UnitsAtLeast(9 * 8, UnitMatchType(Protoss.Interceptor)),
      new ConsiderAttacking,
      new DefendHearts),
    new FollowBuildOrder,
    new Gather
  ))
}
