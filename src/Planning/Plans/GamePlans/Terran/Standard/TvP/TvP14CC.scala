package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.UnitMatchers.UnitMatchSiegeTank
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.{NoPlan, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvP.TvPEarly14CC

class TvP14CC extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvPEarly14CC)
  override val completionCriteria: Predicate = new UnitsAtLeast(1, Terran.SiegeTankSieged)
  
  override val aggression = 0.8
  
  override def defaultPlacementPlan: Plan = new BunkersAtNatural(2)
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(3, UnitMatchSiegeTank, complete = true),
    initialAfter = super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan = NoPlan()
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(9, Terran.SCV),
    Get(1, Terran.SupplyDepot),
    Get(14, Terran.SCV),
    Get(2, Terran.CommandCenter))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(Get(1, Terran.Barracks)),
    new Pump(Terran.SiegeTankUnsieged),
    new Pump(Terran.Marine),
    new PumpWorkers,
    new Build(
      Get(1, Terran.Bunker),
      Get(2, Terran.SupplyDepot),
      Get(1, Terran.Refinery),
      Get(1, Terran.Factory),
      Get(1, Terran.MachineShop),
      Get(Terran.SiegeMode),
      Get(2, Terran.Factory),
      Get(2, Terran.Refinery),
      Get(4, Terran.Factory)))
}
