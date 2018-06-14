package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Reactive.EnemyMutalisks
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Terran.BuildMissileTurretsAtBases
import Planning.Plans.Predicates.Milestones.{IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ.TvZMidgameBio

class TerranVsZergBio extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvZMidgameBio)
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Comsat),
    new TrainContinuously(Terran.ScienceVessel),
    new If(
      new And(
        new UnitsAtLeast(1, Terran.Academy, complete = true),
        new UnitsAtLeast(3, Terran.Marine, complete = true),
        new Check(() => With.units.countOurs(Terran.Marine) > 6 * With.units.countOurs(Terran.Medic))
      ),
      new TrainContinuously(Terran.Medic, 12),
      new TrainContinuously(Terran.Marine)
    ),
    new RequireMiningBases(2),
    new Build(
      GetAtLeast(1, Terran.Refinery),
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(2, Terran.Barracks),
      GetAtLeast(2, Terran.Bunker),
      GetTech(Terran.Stim),
      GetAtLeast(1, Terran.EngineeringBay),
      GetAtLeast(2, Terran.Comsat),
      GetAtLeast(5, Terran.Barracks),
      GetAtLeast(1, Terran.MissileTurret)),
    new BuildGasPumps,
    new If(new EnemyMutalisks, new BuildMissileTurretsAtBases(2)),
    new Build(
      GetUpgrade(Terran.MarineRange),
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(1, Terran.Starport),
      GetAtLeast(1, Terran.ScienceFacility),
      GetAtLeast(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioArmor),
    new UpgradeContinuously(Terran.BioDamage),
    new TrainContinuously(Terran.Comsat),
    new Build(
      GetAtLeast(2, Terran.Starport),
      GetAtLeast(2, Terran.ControlTower),
      GetTech(Terran.Irradiate),
      GetUpgrade(Terran.ScienceVesselEnergy)),
    new RequireMiningBases(3),
    new TrainContinuously(Terran.Vulture),
    new IfOnMiningBases(2, new Build(GetAtLeast(10, Terran.Barracks))),
    new IfOnMiningBases(3, new Build(GetAtLeast(15, Terran.Barracks))),
    new IfOnMiningBases(4, new Build(GetAtLeast(20, Terran.Barracks)))
  )
}