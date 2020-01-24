package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP1ZealotExpand

class PvP1ZealotExpand extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvP1ZealotExpand)
  override val completionCriteria: Predicate = new Latch(new And(new BasesAtLeast(2), new UnitsAtLeast(3, Protoss.Gateway)))

  override def attackPlan: Plan = new If(new EnemyStrategy(With.fingerprints.nexusFirst), new Attack)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)

  override val buildOrderPlan = new Parallel(
    new BuildOrder(
      Get(8,  Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14, Protoss.Probe),
      Get(2,  Protoss.Pylon),
      Get(16, Protoss.Probe)),
    new If(
      new Not(new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.cannonRush)),
      new BuildOrder(
        Get(2,  Protoss.Nexus),
        Get(17, Protoss.Probe))))

  override def buildPlans = Vector(
    new If(
      new EnemiesAtLeast(2, Protoss.Zealot),
      new BuildOrder(Get(2, Protoss.Zealot))),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(3, Protoss.Gateway),
      Get(Protoss.DragoonRange)),
    new PvPIdeas.TrainArmy,
    new RequireMiningBases(2)
  )
}
