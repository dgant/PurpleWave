package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZLateGameReaver

class PvZLateGameReaver extends GameplanModeTemplate {

  override val activationCriteria = new Employing(PvZLateGameReaver)

  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Protoss.Corsair),
    new PvZIdeas.ConditionalAttack)

  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)

  class AddPriorityTech extends Parallel(
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)),
    new IfOnMiningBases(2,
      new Parallel(
        new Build(Get(Protoss.Forge)),
        new BuildOrder(
          Get(5, Protoss.Gateway),
          Get(Protoss.GroundDamage),
          Get(Protoss.GroundArmor),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.RoboticsSupportBay),
          Get(Protoss.ShuttleSpeed),
          Get(Protoss.Observatory),
          Get(6, Protoss.Gateway)),
        new BuildGasPumps)))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new IfOnMiningBases(2, new If(new UnitsAtLeast(3, Protoss.Reaver), new RequireMiningBases(3))),
    new IfOnMiningBases(2, new If(new TechComplete(Protoss.PsionicStorm), new RequireMiningBases(3))),
    new IfOnMiningBases(3, new If(new UnitsAtLeast(10, Protoss.Gateway), new RequireMiningBases(4))),
    new AddPriorityTech,
    new Trigger(
      new GasAtLeast(500),
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives),
        Get(Protoss.PsionicStorm))),
    new PvZIdeas.TrainAndUpgradeArmy,
    new BuildCannonsAtExpansions(5),
    new PvZIdeas.AddGateways,
    new RequireMiningBases(5)
  )
}
