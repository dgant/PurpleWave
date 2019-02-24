package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Reactive.EnemyRobo
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP4GateGoon

class PvP4GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP4GateGoon)
  override val completionCriteria : Predicate = new Latch(new MiningBasesAtLeast(2))

  override def attackPlan: Plan = new PvPIdeas.AttackSafely

  override def scoutPlan: Plan = new ScoutOn(Protoss.Gateway)
  override val workerPlan: Plan = NoPlan()
  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate)
  
  override def buildOrderPlan = new If(
    new Not(new EnemyStrategy(With.fingerprints.cannonRush, With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.dtRush)),
    new BuildOrder(ProtossBuilds.FourGateGoon: _*))

  override val buildPlans = Vector(
    new If(
      new UnitsAtMost(2, Protoss.Gateway),
      new CapGasWorkersAt(2)),

    new If(
      new Or(
        new UnitsAtLeast(15, Protoss.Dragoon),
        new UnitsAtLeast(3, Protoss.DarkTemplar),
        new EnemiesAtLeast(1, Protoss.PhotonCannon)),
      new RequireMiningBases(2)),

    new Pump(Protoss.DarkTemplar),
    new If(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Parallel(
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives)),
        new PvPIdeas.TrainArmy),
      new Pump(Protoss.Dragoon)),

    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(4, Protoss.Gateway)),

    new If(
      new And(
        new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
        new Not(new EnemyRobo)),
      new Build(Get(Protoss.Forge))),

    new RequireMiningBases(2)
  )
}
