package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestTech}
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import Planning.Plan
import Planning.Plans.Compound.{NoPlan, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvP.TvPEarly14CC

class TvP14CC extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvPEarly14CC)
  override val completionCriteria: Plan = new UnitsAtLeast(1, Terran.SiegeTankSieged)
  
  override val aggression = 0.8
  
  override def defaultPlacementPlan: Plan = new BunkersAtNatural(2)
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(3, UnitMatchSiegeTank, complete = true),
    initialAfter = super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan = NoPlan()
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(14, Terran.SCV),
    RequestAtLeast(2, Terran.CommandCenter))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(RequestAtLeast(1, Terran.Barracks)),
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new TrainContinuously(Terran.Marine),
    new TrainWorkersContinuously,
    new Build(
      RequestAtLeast(1, Terran.Bunker),
      RequestAtLeast(2, Terran.SupplyDepot),
      RequestAtLeast(1, Terran.Refinery),
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(1, Terran.MachineShop),
      RequestTech(Terran.SiegeMode),
      RequestAtLeast(2, Terran.Factory),
      RequestAtLeast(2, Terran.Refinery),
      RequestAtLeast(4, Terran.Factory)))
}
