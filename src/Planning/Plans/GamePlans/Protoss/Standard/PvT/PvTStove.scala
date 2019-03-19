package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Predicates.Compound.Latch
import Planning.UnitCounters.UnitCountOne
import Planning.Plans.Army.{ConsiderAttacking, DefendEntrance}
import Planning.Plans.Compound.{If, Or}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones.{EnemyHasShown, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvTStove

class PvTStove extends GameplanTemplate {
  
  override val activationCriteria   = new Employing(PvTStove)
  override val completionCriteria   = new Latch(new Or(new MiningBasesAtLeast(3), new UnitsAtLeast(1, Protoss.ArbiterTribunal)))
  override def priorityAttackPlan   = new PvTIdeas.PriorityAttacks
  override def priorityDefensePlan  = new If(new EnemyHasShown(Terran.Vulture), new DefendEntrance(Protoss.Dragoon, UnitCountOne))
  override def scoutPlan = new ScoutOn(Protoss.Pylon)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvTIdeas.ReactToBBS,
    new PvTIdeas.ReactToWorkerRush)

  override def attackPlan = new If(
      new Latch(new UnitsAtLeast(1, Protoss.Scout, complete = true)),
      new ConsiderAttacking)
  
  override val buildOrder = Vector(
    //CoreZ, Scout @ Pylon -- from Antiga replay
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(14,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(Protoss.Stargate),
    Get(16,  Protoss.Probe),
    Get(Protoss.Dragoon),
    Get(17,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(19,  Protoss.Probe),
    Get(Protoss.Scout),
    Get(Protoss.CitadelOfAdun),
    Get(20,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(21,  Protoss.Probe),
    Get(4,   Protoss.Pylon),
    Get(Protoss.TemplarArchives),
    Get(22,  Protoss.Probe),
    Get(2,   Protoss.Scout),
    Get(24,  Protoss.Probe),
    Get(Protoss.DarkTemplar),
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