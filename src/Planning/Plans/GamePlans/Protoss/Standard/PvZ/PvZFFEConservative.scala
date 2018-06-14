package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plan
import Planning.Plans.Compound.{Latch, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZEarlyFFEConservative

class PvZFFEConservative extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(PvZEarlyFFEConservative)
  override val completionCriteria: Plan = new Latch(new UnitsAtLeast(2, Protoss.PhotonCannon))
  
  override def defaultScoutPlan     : Plan = NoPlan()
  override def defaultWorkerPlan    : Plan = NoPlan()
  override def defaultSupplyPlan    : Plan = NoPlan()
  override def defaultPlacementPlan : Plan = new PlacementForgeFastExpand
  
  override def defaultBuildOrder: Plan = new BuildOrder(ProtossBuilds.FFE_Conservative: _*)
}
