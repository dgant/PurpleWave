package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plans.Army.{ConsiderAttacking, DefendEntrance}
import Planning.Plans.Compound.{If, Or}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{EnemyHasShown, MiningBasesAtLeast, UnitsAtLeast}
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
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.Stargate),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Scout),
    GetAtLeast(1,   Protoss.CitadelOfAdun),
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot),
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.TemplarArchives),
    GetAtLeast(22,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Scout),
    GetAtLeast(24,  Protoss.Probe),
    GetAtLeast(1,   Protoss.DarkTemplar),
    GetAtLeast(25,  Protoss.Probe),
    GetAtLeast(2,  Protoss.Nexus),
    GetAtLeast(26,  Protoss.Probe),
    GetAtLeast(5,   Protoss.Pylon),
    GetAtLeast(27,  Protoss.Probe),
    GetAtLeast(2,   Protoss.DarkTemplar),
    GetAtLeast(28,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Scout),
    GetAtLeast(29,  Protoss.Probe),
    GetAtLeast(3,   Protoss.DarkTemplar))
  
  override def buildPlans = Vector(
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(GetTech(Protoss.PsionicStorm))),
    new PvTIdeas.TrainArmy,
    new BuildGasPumps,
    new If(
      new EnemyHasShown(Terran.SpiderMine),
      new Build(
        GetAtLeast(1, Protoss.RoboticsFacility),
        GetUpgrade(Protoss.DragoonRange),
        GetAtLeast(1, Protoss.Observatory))),
    new Build(
      GetAtLeast(2, Protoss.Gateway),
      GetUpgrade(Protoss.DragoonRange),
      GetAtLeast(1, Protoss.ArbiterTribunal),
      GetAtLeast(6, Protoss.Gateway)),
    new RequireMiningBases(3))
}