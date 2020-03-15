package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.BuildRequests.Get
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvE.PvE15BaseIslandCarrier

class PvE15BaseIslandCarrier extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvE15BaseIslandCarrier)

  override def initialScoutPlan: Plan = NoPlan()
  override def scoutExposPlan: Plan = NoPlan()

  override def attackPlan: Plan = new Trigger(
    new UnitsAtLeast(4, Protoss.Carrier, complete = true),
    super.attackPlan)

  override def placementPlan: Plan = new PlaceIslandPylons

  override def buildOrderPlan: Plan = new BuildOrder(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(11, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(17, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(2, Protoss.Nexus),
    Get(Protoss.Stargate))

  override def buildPlans: Seq[Plan] = Vector(
    new PvEIslandCarrierLateGame
  )
}