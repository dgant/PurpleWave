package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Compound.NoPlan
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.MiningBasesAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateStargate

class PvT1GateReaver extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1GateStargate)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override def priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val superSaturate      = true
  override def defaultAttackPlan  = NoPlan()
  override def scoutAt            = 8
  
  override val buildOrder = Vector(
    //CoreZ, Scout @ Pylon -- from Antiga replay
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.RoboticsFacility))
  
  override def buildPlans = Vector(
    new BuildOrder(RequestAtLeast(1, Protoss.Shuttle)),
    new PvTIdeas.TrainArmy,
    new BuildOrder(
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(1, Protoss.Reaver)),
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(3, Protoss.Gateway)),
    new BuildGasPumps)
}