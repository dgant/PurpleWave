package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plans.Army.{Attack, DefendEntrance}
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Milestones.{EnemyHasShown, UnitsAtLeast}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateStargateTemplar

class PvTStove extends GameplanModeTemplate {
  
  override val activationCriteria   = new Employing(PvTEarly1GateStargateTemplar)
  override val completionCriteria   = new UnitsAtLeast(1, Protoss.ArbiterTribunal)
  override def priorityAttackPlan   = new PvTIdeas.PriorityAttacks
  override def priorityDefensePlan  = new If(new EnemyHasShown(Terran.Vulture), new DefendEntrance { defenders.get.unitMatcher.set(Protoss.Dragoon); defenders.get.unitCounter.set(UnitCountOne)})
  override def defaultAttackPlan    = new Attack
  override def scoutAt              = 9
  
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
    RequestAtLeast(1,   Protoss.Stargate),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Scout),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.TemplarArchives),
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Scout),
    RequestAtLeast(24,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.DarkTemplar),
    RequestAtLeast(25,  Protoss.Probe),
    RequestAtLeast(2,  Protoss.Nexus),
    RequestAtLeast(26,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Pylon),
    RequestAtLeast(27,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.DarkTemplar),
    RequestAtLeast(28,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Scout),
    RequestAtLeast(29,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.DarkTemplar))
  
  override def buildPlans = Vector(
    new PvTIdeas.TrainArmy,
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.ArbiterTribunal),
      RequestAtLeast(4, Protoss.Gateway)
    ))
}