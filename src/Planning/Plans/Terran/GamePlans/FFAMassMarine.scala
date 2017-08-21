package Planning.Plans.Terran.GamePlans

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, OnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran

class FFAMassMarine extends Parallel {
  
  private class UpgradeStuffEarly extends Parallel(
    new BuildGasPumps,
    new Build(
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(2, Terran.EngineeringBay),
      RequestUpgrade(Terran.MarineRange),
      RequestUpgrade(Terran.BioDamage),
      RequestUpgrade(Terran.BioArmor),
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(1, Terran.ScienceFacility)))
  
  private class UpgradeStuffLate extends Parallel(
    new TrainContinuously(Terran.ControlTower),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new OnGasBases(2, new Build(RequestAtLeast(2, Terran.Starport))),
    new OnGasBases(3, new Build(RequestAtLeast(3, Terran.Starport))),
    new OnGasBases(4, new Build(RequestAtLeast(4, Terran.Starport)))
  )
  
  children.set(Vector(
    new RequireBareMinimum,
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(8, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(14, Terran.SCV),
        RequestAtLeast(2, Terran.CommandCenter),
        RequestAtLeast(16, Terran.SCV),
        RequestAtLeast(2, Terran.Barracks),
        RequestAtLeast(18, Terran.SCV),
        RequestAtLeast(4, Terran.Barracks))),
    new RequireMiningBases(2),
    new RequireSufficientSupply,
    new TrainContinuously(Terran.Comsat),
    new TrainWorkersContinuously,
    new TrainContinuously(Terran.ScienceVessel),
    new If(
      new UnitsAtLeast(30, UnitMatchType(Terran.Marine)),
      new UpgradeStuffEarly),
    new If(
      new UnitsAtLeast(40, UnitMatchType(Terran.Marine)),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(50, UnitMatchType(Terran.Marine)),
      new UpgradeStuffLate),
    new If(
      new UnitsAtLeast(60, UnitMatchType(Terran.Marine)),
      new RequireMiningBases(4)),
    new TrainContinuously(Terran.Marine),
    new OnMiningBases(1, new Build(RequestAtLeast(6, Terran.Barracks))),
    new OnMiningBases(2, new Build(RequestAtLeast(12, Terran.Barracks))),
    new OnMiningBases(3, new Build(RequestAtLeast(18, Terran.Barracks))),
    new OnMiningBases(4, new Build(RequestAtLeast(24, Terran.Barracks))),
    new UpgradeStuffEarly,
    new UpgradeStuffLate,
    new Build(RequestAtLeast(24, Terran.Barracks)),
    new Scan,
    new DefendZones,
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}