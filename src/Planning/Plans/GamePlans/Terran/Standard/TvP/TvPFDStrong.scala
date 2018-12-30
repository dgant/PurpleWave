package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvPEarlyFDStrong

class TvPFDStrong extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvPEarlyFDStrong)
  override val completionCriteria: Predicate = new UnitsAtLeast(15, UnitMatchWarriors)
  
  override val aggression = 0.8
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(3, UnitMatchSiegeTank, complete = true),
    initialAfter = super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan: Plan = TvPIdeas.workerPlan
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(10, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(Terran.Barracks),
    Get(Terran.Refinery),
    Get(12, Terran.SCV),
    Get(1, Terran.Marine),
    Get(13, Terran.SCV),
    Get(2, Terran.Marine),
    Get(Terran.Factory),
    Get(14, Terran.SCV),
    Get(2, Terran.SupplyDepot))
  
  override def buildPlans: Seq[Plan] = Vector(
    // TODO: Gas cut
    new Pump(Terran.SiegeTankUnsieged),
    new Pump(Terran.Marine),
    new Build(Get(1, Terran.MachineShop)),
    new If(
      new UnitsAtLeast(2, Terran.Factory, complete = true),
      new Pump(Terran.Vulture)),
    new Build(Get(Terran.SpiderMinePlant)),
    new RequireMiningBases(2),
    new BuildGasPumps,
    new Build(
      Get(4, Terran.Factory),
      Get(3, Terran.MachineShop),
      Get(Terran.VultureSpeed),
      Get(6, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(
      Get(1, Terran.Academy),
      Get(2, Terran.Armory),
      Get(1, Terran.Starport)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(1, Terran.ScienceFacility),
      Get(8, Terran.Factory),
      Get(5, Terran.MachineShop)),
    new RequireMiningBases(4),
  new Build(
    Get(12, Terran.Factory),
    Get(5, Terran.MachineShop)))
}
