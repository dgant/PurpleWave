package Planning.Plans.Terran.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.EnemyMutalisks
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Terran

class TerranVsZergBio extends Parallel {
  
  children.set(Vector(
    new RequireSufficientSupply,
    new TrainContinuously(Terran.Comsat),
    new TrainWorkersContinuously,
    new TrainContinuously(Terran.ScienceVessel),
    new If(
      new And(
        new UnitsAtLeast(1, Terran.Academy, complete = true),
        new UnitsAtLeast(3, Terran.Marine, complete = true),
        new Check(() => With.units.ours.count(_.is(Terran.Marine)) > 6 * With.units.ours.count(_.is(Terran.Medic)))
      ),
      new TrainContinuously(Terran.Medic, 12),
      new TrainContinuously(Terran.Marine)
    ),
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(1, Terran.Refinery),
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(2, Terran.Barracks),
      RequestAtLeast(2, Terran.Bunker),
      RequestTech(Terran.Stim),
      RequestAtLeast(1, Terran.EngineeringBay),
      RequestAtLeast(2, Terran.Comsat),
      RequestAtLeast(5, Terran.Barracks),
      RequestAtLeast(1, Terran.MissileTurret)),
    new BuildGasPumps,
    new If(new EnemyMutalisks, new BuildMissileTurretsAtBases(2)),
    new Build(
      RequestUpgrade(Terran.MarineRange),
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioArmor),
    new UpgradeContinuously(Terran.BioDamage),
    new TrainContinuously(Terran.Comsat),
    new Build(
      RequestAtLeast(2, Terran.Starport),
      RequestAtLeast(2, Terran.ControlTower),
      RequestTech(Terran.Irradiate),
      RequestUpgrade(Terran.ScienceVesselEnergy)),
    new RequireMiningBases(3),
    new TrainContinuously(Terran.Vulture),
    new OnMiningBases(2, new Build(RequestAtLeast(10,  Terran.Barracks))),
    new OnMiningBases(3, new Build(RequestAtLeast(15, Terran.Barracks))),
    new OnMiningBases(4, new Build(RequestAtLeast(20, Terran.Barracks))),
    new ScoutAt(16),
    new DefendZones,
    new ConsiderAttacking
  ))
}