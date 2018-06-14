package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.{BuildRequest, GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvP.TvPEarlyFDStrong

class TvPFDStrong extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvPEarlyFDStrong)
  override val completionCriteria: Plan = new UnitsAtLeast(15, UnitMatchWarriors)
  
  override val aggression = 0.8
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(3, UnitMatchSiegeTank, complete = true),
    initialAfter = super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan: Plan = TvPIdeas.workerPlan
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    GetAtLeast(10, Terran.SCV),
    GetAtLeast(1, Terran.SupplyDepot),
    GetAtLeast(1, Terran.Barracks),
    GetAtLeast(1, Terran.Refinery),
    GetAtLeast(12, Terran.SCV),
    GetAtLeast(1, Terran.Marine),
    GetAtLeast(13, Terran.SCV),
    GetAtLeast(2, Terran.Marine),
    GetAtLeast(1, Terran.Factory),
    GetAtLeast(14, Terran.SCV),
    GetAtLeast(2, Terran.SupplyDepot))
  
  override def buildPlans: Seq[Plan] = Vector(
    // TODO: Gas cut
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new TrainContinuously(Terran.Marine),
    new Build(GetAtLeast(1, Terran.MachineShop)),
    new If(
      new UnitsAtLeast(2, Terran.Factory, complete = true),
      new TrainContinuously(Terran.Vulture)),
    new Build(GetTech(Terran.SpiderMinePlant)),
    new RequireMiningBases(2),
    new BuildGasPumps,
    new Build(
      GetAtLeast(4, Terran.Factory),
      GetAtLeast(3, Terran.MachineShop),
      GetUpgrade(Terran.VultureSpeed),
      GetAtLeast(6, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(2, Terran.Armory),
      GetAtLeast(1, Terran.Starport)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      GetAtLeast(1, Terran.ScienceFacility),
      GetAtLeast(8, Terran.Factory),
      GetAtLeast(5, Terran.MachineShop)),
    new RequireMiningBases(4),
  new Build(
    GetAtLeast(12, Terran.Factory),
    GetAtLeast(5, Terran.MachineShop)))
}
