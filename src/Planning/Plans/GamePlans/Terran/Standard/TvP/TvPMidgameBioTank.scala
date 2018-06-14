package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound.{Check, If, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvP.TvPMidgameBioTank

class TvPMidgameBioTank extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvPMidgameBioTank)
  
  override val aggression = 0.8
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(50, UnitMatchWarriors),
    super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan: Plan = TvPIdeas.workerPlan
  
  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new TrainContinuously(Terran.MachineShop),
    
    new If(
      new UnitsAtLeast(50, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine) / 7 >
        With.units.countOurs(Terran.Medic)),
      new TrainContinuously(Terran.Medic, 8, 2)),
    new TrainContinuously(Terran.Marine),
    new Build(
      Get(1, Terran.Barracks),
      Get(1, Terran.Factory),
      Get(Terran.SiegeMode),
      Get(1, Terran.EngineeringBay),
      Get(2, Terran.Barracks),
      Get(1, Terran.MissileTurret)),
    new BuildGasPumps,
    new Build(
      Get(2, Terran.Factory),
      Get(Terran.BioDamage),
      Get(1, Terran.Academy),
      Get(Terran.Stim),
      Get(Terran.MarineRange),
      Get(Terran.BioArmor),
      Get(4, Terran.Barracks),
      Get(6, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(
      Get(1, Terran.Starport),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(1, Terran.ScienceFacility),
      Get(8, Terran.Factory)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor))
}
