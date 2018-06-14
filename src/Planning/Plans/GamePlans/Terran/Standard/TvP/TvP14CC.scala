package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.{BuildRequest, GetAtLeast, GetTech}
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import Planning.Plan
import Planning.Plans.Compound.{NoPlan, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
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
    GetAtLeast(9, Terran.SCV),
    GetAtLeast(1, Terran.SupplyDepot),
    GetAtLeast(14, Terran.SCV),
    GetAtLeast(2, Terran.CommandCenter))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(GetAtLeast(1, Terran.Barracks)),
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new TrainContinuously(Terran.Marine),
    new TrainWorkersContinuously,
    new Build(
      GetAtLeast(1, Terran.Bunker),
      GetAtLeast(2, Terran.SupplyDepot),
      GetAtLeast(1, Terran.Refinery),
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(1, Terran.MachineShop),
      GetTech(Terran.SiegeMode),
      GetAtLeast(2, Terran.Factory),
      GetAtLeast(2, Terran.Refinery),
      GetAtLeast(4, Terran.Factory)))
}
