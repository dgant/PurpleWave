package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.Check
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Predicates.Employing
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvP.TvPMidgameBioTank

class TvPMidgameBioTank extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvPMidgameBioTank)
  
  override val aggression = 0.8
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(50, UnitMatchWarriors),
    super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan: Plan = TvPIdeas.workerPlan
  
  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new Pump(Terran.MachineShop),
    
    new If(
      new UnitsAtLeast(50, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new Pump(Terran.SiegeTankUnsieged),
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine) / 7 >
        With.units.countOurs(Terran.Medic)),
      new Pump(Terran.Medic, 8, 2)),
    new Pump(Terran.Marine),
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
