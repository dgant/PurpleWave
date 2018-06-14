package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.BuildRequests.{Get, Tech, Upgrade}
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
      Get(1, Terran.Refinery),
      Get(1, Terran.Academy),
      Get(2, Terran.Barracks),
      Get(2, Terran.Bunker),
      Tech(Terran.Stim),
      Get(1, Terran.EngineeringBay),
      Get(2, Terran.Comsat),
      Get(5, Terran.Barracks),
      Get(1, Terran.MissileTurret)),
    new BuildGasPumps,
    new If(new EnemyMutalisks, new BuildMissileTurretsAtBases(2)),
    new Build(
      Upgrade(Terran.MarineRange),
      Get(1, Terran.Factory),
      Get(1, Terran.Starport),
      Get(1, Terran.ScienceFacility),
      Get(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioArmor),
    new UpgradeContinuously(Terran.BioDamage),
    new TrainContinuously(Terran.Comsat),
    new Build(
      Get(2, Terran.Starport),
      Get(2, Terran.ControlTower),
      Tech(Terran.Irradiate),
      Upgrade(Terran.ScienceVesselEnergy)),
    new RequireMiningBases(3),
    new TrainContinuously(Terran.Vulture),
    new IfOnMiningBases(2, new Build(Get(10, Terran.Barracks))),
    new IfOnMiningBases(3, new Build(Get(15, Terran.Barracks))),
    new IfOnMiningBases(4, new Build(Get(20, Terran.Barracks)))
  )
}