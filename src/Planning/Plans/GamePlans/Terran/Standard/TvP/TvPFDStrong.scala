package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
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
    RequestAtLeast(10, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(1, Terran.Refinery),
    RequestAtLeast(12, Terran.SCV),
    RequestAtLeast(1, Terran.Marine),
    RequestAtLeast(13, Terran.SCV),
    RequestAtLeast(2, Terran.Marine),
    RequestAtLeast(1, Terran.Factory),
    RequestAtLeast(14, Terran.SCV),
    RequestAtLeast(2, Terran.SupplyDepot))
  
  override def buildPlans: Seq[Plan] = Vector(
    // TODO: Gas cut
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new TrainContinuously(Terran.Marine),
    new Build(RequestAtLeast(1, Terran.MachineShop)),
    new If(
      new UnitsAtLeast(2, Terran.Factory, complete = true),
      new TrainContinuously(Terran.Vulture)),
    new Build(RequestTech(Terran.SpiderMinePlant)),
    new RequireMiningBases(2),
    new BuildGasPumps,
    new Build(
      RequestAtLeast(4, Terran.Factory),
      RequestAtLeast(3, Terran.MachineShop),
      RequestUpgrade(Terran.VultureSpeed),
      RequestAtLeast(6, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(2, Terran.Armory),
      RequestAtLeast(1, Terran.Starport)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(8, Terran.Factory),
      RequestAtLeast(5, Terran.MachineShop)),
    new RequireMiningBases(4),
  new Build(
    RequestAtLeast(12, Terran.Factory),
    RequestAtLeast(5, Terran.MachineShop)))
}
