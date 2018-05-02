package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
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
      RequestAtLeast(1, Terran.Barracks),
      RequestAtLeast(1, Terran.Factory),
      RequestTech(Terran.SiegeMode),
      RequestAtLeast(1, Terran.EngineeringBay),
      RequestAtLeast(2, Terran.Barracks),
      RequestAtLeast(1, Terran.MissileTurret)),
    new BuildGasPumps,
    new Build(
      RequestAtLeast(2, Terran.Factory),
      RequestUpgrade(Terran.BioDamage),
      RequestAtLeast(1, Terran.Academy),
      RequestTech(Terran.Stim),
      RequestUpgrade(Terran.MarineRange),
      RequestUpgrade(Terran.BioArmor),
      RequestAtLeast(4, Terran.Barracks),
      RequestAtLeast(6, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(8, Terran.Factory)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor))
}
