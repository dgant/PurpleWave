package Planning.Plans.Terran.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildRefineries, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, OnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Terran

class TerranVsTerran extends Parallel {
  
  children.set(Vector(
    new RequireMiningBases(1),
    new FirstEightMinutes(
      //1 Fact FE
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(9, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(12, Terran.SCV),
        RequestAtLeast(1, Terran.Barracks),
        RequestAtLeast(1, Terran.Refinery),
        RequestAtLeast(15, Terran.SCV),
        RequestAtLeast(2, Terran.SupplyDepot),
        RequestAtLeast(16, Terran.SCV),
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(20, Terran.SCV))),
    new If(
      new UnitsAtLeast(1, UnitMatchSiegeTank),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(30, UnitMatchWarriors),
      new RequireMiningBases(4)),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new BuildRefineries,
    new TrainContinuously(Terran.Comsat),
    new TrainContinuously(Terran.Battlecruiser),
    new TrainContinuously(Terran.SiegeTankUnsieged, 30),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Terran.Armory)),
      new Build(RequestAtLeast(1, Terran.ControlTower))
    ),
    new TrainMatchingRatio(Terran.Valkyrie, 3, Seq(MatchingRatio(UnitMatchType(Terran.Wraith), 0.5))),
    new TrainContinuously(Terran.Wraith),
    new Build(
      RequestAtLeast(1, Terran.Barracks),
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(1, Terran.MachineShop),
      RequestAtLeast(1, Terran.Starport),
      RequestTech(Terran.SiegeMode)),
    new TrainContinuously(Terran.Marine),
    new If(
      new And(
        new UnitsAtLeast(1, UnitMatchType(Terran.Armory), complete = true),
        new Check(() => With.self.gas > 200)),
      new TrainContinuously(Terran.Goliath),
      new TrainContinuously(Terran.Vulture)),
    new OnMiningBases(2, new Build(
      RequestAtLeast(3, Terran.Factory),
      RequestUpgrade(Terran.VultureSpeed),
      RequestAtLeast(5, Terran.Factory),
      RequestTech(Terran.SpiderMinePlant),
      RequestAtLeast(1, Terran.Academy))),
    new OnMiningBases(3, new Build(
      RequestAtLeast(8, Terran.Factory))),
    new OnMiningBases(4, new Build(
      RequestAtLeast(12, Terran.Factory))),
    new OnGasBases(1, new Build(
      RequestAtLeast(1, Terran.MachineShop))),
    new OnGasBases(2, new Build(
      RequestAtLeast(3, Terran.MachineShop),
      RequestAtLeast(1, Terran.Armory))),
    new OnGasBases(3, new Build(
      RequestAtLeast(5, Terran.MachineShop))),
    new OnGasBases(3, new Build(
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(2, Terran.Starport))),
    new OnGasBases(4, new Build(
      RequestAtLeast(1, Terran.PhysicsLab),
      RequestAtLeast(4, Terran.Starport),
      RequestAtLeast(4, Terran.ControlTower),
      RequestUpgrade(Terran.BattlecruiserEnergy),
      RequestTech(Terran.Yamato))),
    new UpgradeContinuously(Terran.MechDamage),
    new RequireMiningBases(3),
    new ScoutAt(14),
    new ControlMap,
    new Trigger(
      new UnitsAtLeast(3, UnitMatchSiegeTank, complete = true),
      new ConsiderAttacking
    ),
    new DefendChokes,
    new FollowBuildOrder,
    new Scan,
    new RemoveMineralBlocksAt(40),
    new Gather
  ))
}