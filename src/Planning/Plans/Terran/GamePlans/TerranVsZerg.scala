package Planning.Plans.Terran.GamePlans

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.{EnemyLurkers, EnemyMutalisks}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Terran

class TerranVsZerg extends Parallel {
  
  children.set(Vector(
    new RequireMiningBases(1),
    new ProposePlacement {
      override lazy val blueprints: Iterable[Blueprint] = Vector(
        new Blueprint(this, building = Some(Terran.Bunker), preferZone = With.geography.ourNatural.map(_.zone))
      )
    },
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(9, Terran.SCV),
        RequestAtLeast(1, Terran.Barracks),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(10, Terran.SCV),
        RequestAtLeast(1, Terran.Bunker),
        RequestAtLeast(1, Terran.Marine),
        RequestAtLeast(12, Terran.SCV),
        RequestAtLeast(1, Terran.Marine))),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Terran.ScienceVessel),
    new If(
      new And(
        new UnitsAtLeast(1, UnitMatchType(Terran.Academy), complete = true),
        new UnitsAtLeast(3, UnitMatchType(Terran.Marine), complete = true),
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
      RequestTech(Terran.Stim),
      RequestAtLeast(1, Terran.EngineeringBay),
      RequestAtLeast(2, Terran.Comsat),
      RequestAtLeast(5, Terran.Barracks)),
    new BuildRefineries,
    new If(new EnemyLurkers, new BuildMissileTurretsAtBases(1)),
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
    new Build(RequestAtLeast(16, Terran.Barracks)),
    new ScoutAt(14),
    new DefendZones,
    new If(
      new UnitsAtLeast(25, UnitMatchWarriors),
      new ConsiderAttacking),
    new FollowBuildOrder,
    new Scan,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new RecruitFreelancers
  ))
}