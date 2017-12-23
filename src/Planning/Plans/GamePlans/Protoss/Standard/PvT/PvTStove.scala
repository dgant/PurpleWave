package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plans.Army.{Attack, DefendEntrance}
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{EnemyHasShown, UnitsAtLeast}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateStargateTemplar

class PvTStove extends GameplanModeTemplate {
  
  override val activationCriteria   = new Employing(PvTEarly1GateStargateTemplar)
  override val completionCriteria   = new UnitsAtLeast(1, Protoss.ArbiterTribunal)
  override def priorityAttackPlan   = new PvTIdeas.PriorityAttacks
  override def priorityDefensePlan  = new If(new EnemyHasShown(Terran.Vulture), new DefendEntrance { defenders.get.unitMatcher.set(Protoss.Dragoon); defenders.get.unitCounter.set(UnitCountOne)})
  override def defaultAttackPlan    = new Attack
  override def scoutAt              = 8
  
  override val buildOrder = Vector(
    //ZCoreZ, Scout @ Pylon -- from Antiga replay
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
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Scout),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.TemplarArchives),
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.DarkTemplar),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(24,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.DarkTemplar),
    RequestAtLeast(25,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(5,   Protoss.Pylon),
    RequestAtLeast(26,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(3,   Protoss.Dragoon))
  
  override def buildPlans = Vector(
    new BuildGasPumps,
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.ArbiterTribunal)),
    new PvTIdeas.TrainArmy)
}