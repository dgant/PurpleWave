package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Predicates.Compound.Latch
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Predicates.Employing
import Planning.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZEarlyFFEConservative

class PvZFFEConservative extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(PvZEarlyFFEConservative)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Protoss.PhotonCannon))
  
  override def defaultScoutPlan     : Plan = NoPlan()
  override def defaultWorkerPlan    : Plan = NoPlan()
  override def defaultSupplyPlan    : Plan = NoPlan()
  override def defaultPlacementPlan : Plan = new PlacementForgeFastExpand
  
  override def defaultBuildOrder: Plan = new BuildOrder(ProtossBuilds.FFE_Conservative: _*)
}
