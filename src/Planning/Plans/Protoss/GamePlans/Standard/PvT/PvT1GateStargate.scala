package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.MiningBasesAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateStargate

class PvT1GateStargate extends TemplateMode {
  
  override val activationCriteria = new Employing(PvTEarly1GateStargate)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override def priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override def scoutExpansionsAt  = 8
  
  override val buildOrder = Vector(
    //ZCoreZ, Scout @ Pylon
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Stargate),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Scout))
  
  override def buildPlans = Vector(
    new PvTIdeas.TrainScouts,
    new TrainContinuously(Protoss.Dragoon),
    new RequireMiningBases(2),
    new PvTIdeas.BuildSecondGasIfWeNeedIt)
}