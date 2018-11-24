package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Predicates.Milestones.{IfOnMiningBases, TechComplete, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class PvZLateGame extends GameplanModeTemplate {

  override def aggression: Double = 0.85

  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Protoss.Corsair),
    new Attack(Protoss.DarkTemplar),
    new PvZIdeas.ConditionalAttack)

  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers, new PvZIdeas.ReactToMutalisks)

  class AddPriorityTech extends Parallel(
    new Build(
      Get(1, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)),
    new If(
      new UnitsAtLeast(1, Protoss.Dragoon),
      new Build(Get(Protoss.DragoonRange))),
    new IfOnMiningBases(2,
      new Parallel(
        new Build(Get(1, Protoss.Forge)),
        new BuildGasPumps,
        new BuildOrder(
          Get(1, Protoss.CitadelOfAdun),
          Get(Protoss.GroundDamage),
          Get(Protoss.ZealotSpeed),
          Get(1, Protoss.TemplarArchives),
          Get(Protoss.DragoonRange),
          Get(Protoss.PsionicStorm),
          Get(4, Protoss.Gateway)))))

  class AddTech extends Parallel(
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory),
      Get(6, Protoss.Gateway)),
    new IfOnMiningBases(3,
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Forge),
        Get(Protoss.HighTemplarEnergy),
        Get(1, Protoss.RoboticsSupportBay),
        Get(Protoss.ScarabDamage))))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new IfOnMiningBases(2, new If(new TechComplete(Protoss.PsionicStorm), new RequireMiningBases(3))),
    new IfOnMiningBases(3, new If(new UnitsAtLeast(10, Protoss.Gateway), new RequireMiningBases(4))),
    new AddPriorityTech,
    new PvZIdeas.TrainAndUpgradeArmy,
    new BuildCannonsAtExpansions(5),
    new AddTech,
    new PvZIdeas.AddGateways,
    new RequireMiningBases(5)
  )
}
