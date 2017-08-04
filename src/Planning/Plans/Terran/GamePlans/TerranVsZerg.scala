package Planning.Plans.Terran.GamePlans

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAtLeast, RequestTech}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildRefineries, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Terran

class TerranVsZerg extends Parallel {
  
  children.set(Vector(
    new RequireMiningBases(1),
    new ProposePlacement {
      override lazy val blueprints: Iterable[Blueprint] = Vector(
        new Blueprint(this, building = Some(Terran.SupplyDepot),  placementProfile = Some(PlacementProfiles.hugTownHall)),
        new Blueprint(this, building = Some(Terran.Barracks),     placementProfile = Some(PlacementProfiles.hugTownHall)),
        new Blueprint(this, building = Some(Terran.Barracks),     placementProfile = Some(PlacementProfiles.hugTownHall))
      )
    },
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(9, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(11, Terran.SCV),
        RequestAtLeast(1, Terran.Barracks),
        RequestAtLeast(13, Terran.SCV),
        RequestAtLeast(2, Terran.Barracks))),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Terran.ScienceVessel, 2),
    new TrainContinuously(Terran.SiegeTankUnsieged, 4),
    new TrainContinuously(Terran.Marine),
    new RequireMiningBases(2),
    new BuildRefineries,
    new Build(RequestAtLeast(1, Terran.Factory)),
    new Build(RequestAtLeast(1, Terran.MachineShop)),
    new Build(RequestAtLeast(1, Terran.EngineeringBay)),
    new Build(RequestAtLeast(1, Terran.Academy)),
    new Build(RequestTech(Terran.SiegeMode)),
    new Build(RequestAtLeast(2, Terran.MissileTurret)),
    new Build(RequestAtLeast(1, Terran.Starport)),
    new TrainContinuously(Terran.Comsat),
    new Build(RequestAtLeast(1, Terran.ScienceFacility)),
    new Build(RequestAtLeast(1, Terran.ControlTower)),
    new Build(RequestAtLeast(4, Terran.Barracks)),
    new RequireMiningBases(3),
    new TrainContinuously(Terran.Wraith),
    new Build(RequestAtLeast(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.MarineRange),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new TrainContinuously(Terran.Barracks),
    new ScoutAt(14),
    new DefendZones,
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new ConsiderAttacking),
    new FollowBuildOrder,
    new Scan,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new Recruit
  ))
}