package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.Get
import Planning.Predicates.Compound.Latch
import Planning.UnitCounters.UnitCountOne
import Planning.Plans.Army.{ConsiderAttacking, DefendEntrance}
import Planning.Plans.Compound.{If, Or}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Employing
import Planning.Predicates.Milestones.{EnemyHasShown, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvTEarly1GateStargateTemplar

class PvTStove extends GameplanModeTemplate {
  
  override val activationCriteria   = new Employing(PvTEarly1GateStargateTemplar)
  override val completionCriteria   = new Latch(new Or(new MiningBasesAtLeast(3), new UnitsAtLeast(1, Protoss.ArbiterTribunal)))
  override def priorityAttackPlan   = new PvTIdeas.PriorityAttacks
  override def priorityDefensePlan  = new If(new EnemyHasShown(Terran.Vulture), new DefendEntrance(Protoss.Dragoon, UnitCountOne))
  override def defaultScoutPlan     = new ScoutOn(Protoss.Pylon)
  
  override def defaultAttackPlan = new If(
      new Latch(new UnitsAtLeast(1, Protoss.Scout, complete = true)),
      new ConsiderAttacking)
  
  override val buildOrder = Vector(
    //CoreZ, Scout @ Pylon -- from Antiga replay
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(1,   Protoss.Stargate),
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),
    Get(17,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(19,  Protoss.Probe),
    Get(1,   Protoss.Scout),
    Get(1,   Protoss.CitadelOfAdun),
    Get(20,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(21,  Protoss.Probe),
    Get(4,   Protoss.Pylon),
    Get(1,   Protoss.TemplarArchives),
    Get(22,  Protoss.Probe),
    Get(2,   Protoss.Scout),
    Get(24,  Protoss.Probe),
    Get(1,   Protoss.DarkTemplar),
    Get(25,  Protoss.Probe),
    Get(2,  Protoss.Nexus),
    Get(26,  Protoss.Probe),
    Get(5,   Protoss.Pylon),
    Get(27,  Protoss.Probe),
    Get(2,   Protoss.DarkTemplar),
    Get(28,  Protoss.Probe),
    Get(3,   Protoss.Scout),
    Get(29,  Protoss.Probe),
    Get(3,   Protoss.DarkTemplar))
  
  override def buildPlans = Vector(
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(Get(Protoss.PsionicStorm))),
    new PvTIdeas.TrainArmy,
    new BuildGasPumps,
    new If(
      new EnemyHasShown(Terran.SpiderMine),
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(Protoss.DragoonRange),
        Get(1, Protoss.Observatory))),
    new Build(
      Get(2, Protoss.Gateway),
      Get(Protoss.DragoonRange),
      Get(1, Protoss.ArbiterTribunal),
      Get(6, Protoss.Gateway)),
    new RequireMiningBases(3))
}