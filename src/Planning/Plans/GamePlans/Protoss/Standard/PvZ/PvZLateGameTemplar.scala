package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.BuildCannonsAtExpansions
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZLateGameTemplar

class PvZLateGameTemplar extends GameplanTemplate {

  override val activationCriteria = new Employing(PvZLateGameTemplar)

  override def attackPlan: Plan = new PvZIdeas.ConditionalAttack

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.ReactToLurkers,
    new PvZIdeas.ReactToMutalisks)

  override def archonPlan: Plan = new PvZIdeas.TemplarUpToEight

  class AddPriorityTech extends Parallel(
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new If(new UnitsAtLeast(2, Protoss.Dragoon), new Build(Get(Protoss.DragoonRange))),
    new IfOnMiningBases(2,
      new Parallel(
        new Build(Get(Protoss.Forge)),
        new BuildGasPumps,
        new BuildOrder(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.GroundDamage),
          Get(Protoss.ZealotSpeed),
          Get(Protoss.TemplarArchives)),
        new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
        new If(new UpgradeComplete(Protoss.ArbiterEnergy), new Build(Get(Protoss.ArbiterTribunal))),
        new If(new UnitsAtLeast(1, Protoss.Arbiter), new Build(Get(Protoss.Stasis))),
        new Build(Get(5, Protoss.Gateway)))))

  class AddTech extends Parallel(
    new Build(
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory),
      Get(6, Protoss.Gateway)),
    new If(
      new GasPumpsAtLeast(3),
      new Parallel(
        new Build(Get(2, Protoss.Forge)),
        new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.HighTemplarEnergy))),
        new If(
          new UnitsAtLeast(8, Protoss.Corsair),
          new Build(
            Get(Protoss.FleetBeacon),
            Get(Protoss.DisruptionWeb))))))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new IfOnMiningBases(2, new If(new UnitsAtLeast(3, Protoss.Reaver), new RequireMiningBases(3))),
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
