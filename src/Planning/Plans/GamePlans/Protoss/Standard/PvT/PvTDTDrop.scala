package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Army.DropAttack
import Planning.Plans.Compound.NoPlan
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarlyDTDrop

class PvTDTDrop extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarlyDTDrop)
  override val completionCriteria = new Latch(new UnitsAtLeast(2, Protoss.DarkTemplar))
  override val superSaturate      = true
  override def defaultAttackPlan  = NoPlan()
  
  override def priorityAttackPlan: Plan = new DropAttack
  
  override val buildOrder = Vector(
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(18,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(24,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(25,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(1,   Protoss.RoboticsFacility))
  
  override def buildPlans = Vector(
    new BuildOrder(
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(1, Protoss.Shuttle)),
    new TrainContinuously(Protoss.DarkTemplar, 2),
    new TrainContinuously(Protoss.Dragoon),
    new BuildOrder(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(4, Protoss.Gateway)))
}