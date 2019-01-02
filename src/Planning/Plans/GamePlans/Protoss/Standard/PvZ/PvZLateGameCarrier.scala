package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural, MeldArchons}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZLateGameCarrier

class PvZLateGameCarrier extends GameplanModeTemplate {

  override val activationCriteria = new Employing(PvZLateGameCarrier)

  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Protoss.Corsair),
    new If(
      new Or(
        new UnitsAtLeast(80, Protoss.Interceptor),
        new MiningBasesAtMost(1),
        new BasesAtLeast(4)),
      new PvZIdeas.ConditionalAttack))

  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)
  override def defaultArchonPlan: Plan = new MeldArchons(49) { override def maximumTemplar = 12 }

  class AddPriorityTech extends Parallel(
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new If(new UnitsAtLeast(1, Protoss.Dragoon), new Build(Get(Protoss.DragoonRange))),
    new IfOnMiningBases(2,
      new Parallel(
        new BuildGasPumps,
        new BuildOrder(
          Get(Protoss.Stargate),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.RoboticsSupportBay),
          Get(Protoss.ShuttleSpeed)),
        new If(new UnitsAtLeast(2, Protoss.Carrier), new Build(Get(Protoss.CarrierCapacity))),
        new If(new UnitsAtLeast(2, Protoss.Dragoon), new Build(Get(Protoss.DragoonRange))),
        new Build(Get(5, Protoss.Gateway)))))

  class AddTech extends If(
    new GasPumpsAtLeast(3),
    new Parallel(
      new Build(
        Get(Protoss.FleetBeacon),
        Get(3, Protoss.Stargate)),
      new If(new UnitsAtLeast(6, Protoss.Corsair), new Build(Get(Protoss.DisruptionWeb))),
      new If(
        new UnitsAtLeast(8, Protoss.Corsair),
        new Build(
          Get(Protoss.FleetBeacon),
          Get(Protoss.DisruptionWeb))),
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.ZealotSpeed))))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new If(new UnitsAtLeast(4, Protoss.Reaver), new RequireMiningBases(3)),
    new If(new UnitsAtLeast(30, Protoss.Interceptor), new RequireMiningBases(3)),
    new AddPriorityTech,
    new Trigger(
      new GasAtLeast(1000),
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives),
        Get(Protoss.PsionicStorm))),
    new PvZIdeas.TrainAndUpgradeArmy,
    new BuildCannonsAtExpansions(6),
    new BuildCannonsAtNatural(5),
    new AddTech,
    new RequireMiningBases(4),
    new PvZIdeas.AddGateways,
  )
}
