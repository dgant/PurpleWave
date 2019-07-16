package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.BuildRequests.Get
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Protoss.BuildCannonsAtBases
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyIsZerg}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvE.PvE2BaseIslandCarrier

class PvE2BaseIslandCarrier extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvE2BaseIslandCarrier)

  override def scoutPlan: Plan = NoPlan()
  override def scoutExposPlan: Plan = NoPlan()

  override def attackPlan: Plan = new Trigger(
    new UnitsAtLeast(4, Protoss.Carrier, complete = true),
    super.attackPlan)

  override def placementPlan: Plan = new PlaceIslandPylons

  override def buildOrderPlan: Plan = new BuildOrder(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(13, Protoss.Probe),
    Get(2, Protoss.Nexus),
    Get(14, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(15, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(16, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(17, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(18, Protoss.Probe),
    Get(Protoss.Forge),
    Get(19, Protoss.Probe),
    Get(2, Protoss.Assimilator))

  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new EnemyIsZerg,
      new BuildCannonsAtBases(3)),
    new PvEIslandCarrierLateGame
  )
}
