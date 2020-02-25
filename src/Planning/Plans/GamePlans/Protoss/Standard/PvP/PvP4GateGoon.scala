package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP4GateGoon

class PvP4GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP4GateGoon)
  override val completionCriteria : Predicate = new Latch(
    new And(
      new MiningBasesAtLeast(2),
      new Or(
        new UnitsAtMost(0, Protoss.TemplarArchives),
        new And(
          new UnitsAtLeast(6, Protoss.Gateway),
          new UnitsAtLeast(2, Protoss.Assimilator)))))

  override def attackPlan: Plan = new Parallel(
    new If(
      new Or(
        new UnitsAtMost(0, Protoss.CitadelOfAdun),
        new Latch(new UnitsAtLeast(1, Protoss.DarkTemplar))),
      new PvPIdeas.AttackSafely),
    new Attack(Protoss.DarkTemplar))

  override def scoutPlan: Plan = new ScoutOn(Protoss.Gateway)
  override val workerPlan: Plan = NoPlan()
  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)
  
  override def buildOrderPlan = new If(
    new Not(new EnemyStrategy(With.fingerprints.cannonRush, With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.dtRush)),
    new BuildOrder(ProtossBuilds.FourGateGoon: _*))

  override val buildPlans = Vector(
    new If(new UnitsAtMost(3, Protoss.Gateway), new CapGasWorkersAt(2)),

    new If(
      new Or(
        new UnitsAtLeast(18, Protoss.Dragoon),
        new EnemiesAtLeast(3, Protoss.PhotonCannon, complete = true)),
      new RequireMiningBases(2)),

    new Trigger(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Parallel(
        new BuildOrder(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives)),
        // At the point where we are training the archives, we need to save a bit of gas to make DTs,
        // so we make Zealots instead
        new If(
          new UnitsAtLeast(1, Protoss.TemplarArchives),
          new BuildOrder(
            Get(2, Protoss.Zealot),
            Get(2, Protoss.DarkTemplar),
            Get(Protoss.Forge))),
        new Trigger(
          new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true),
          new Build(
            Get(2, Protoss.Nexus),
            Get(Protoss.Forge),
            Get(5, Protoss.Gateway))),
        new Trigger(new UnitsAtLeast(2, Protoss.DarkTemplar), new PumpWorkers(oversaturate = true)),
        new Pump(Protoss.Dragoon),
        new BuildCannonsAtNatural(2),
        new Build(
          Get(6, Protoss.Gateway),
          Get(2, Protoss.Assimilator)),
        new Trigger(
          new UnitsAtLeast(2, Protoss.Nexus),
          new PvPIdeas.TrainArmy)),
      new Pump(Protoss.Dragoon)),

    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(4, Protoss.Gateway)),

    new RequireMiningBases(2),
    new Build(
      Get(Protoss.Forge),
      Get(6, Protoss.Gateway),
      Get(2, Protoss.Assimilator))
  )
}
