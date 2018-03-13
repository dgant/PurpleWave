package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Scouting.FoundEnemyBase
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen1GateReaverExpand

class PvP1GateReaverExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1GateReaverExpand)
  override val completionCriteria : Plan      = new Latch(new And(new MiningBasesAtLeast(2), new UnitsAtLeast(3, Protoss.Gateway)))
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override val defaultAttackPlan: Plan = new If(
    new And(
      new EnemyUnitsAtMost(0, UnitMatchWarriors),
      new FoundEnemyBase),
    new Attack)
  
  override val buildOrder = ProtossBuilds.OpeningZCoreZ
  override def buildPlans = Vector(
    
    new If(
      new UnitsAtLeast(2, Protoss.Reaver, complete = true),
      new RequireMiningBases(2)),
    
    new PvPIdeas.TrainArmy,
  
    new If(
      new EnemyStrategy(With.intelligence.fingerprints.fingerprintProxyGateway),
      new Build(RequestAtLeast(3, Protoss.Gateway))),
    new If(
      new EnemyStrategy(With.intelligence.fingerprints.fingerprint2Gate),
      new Build(RequestAtLeast(2, Protoss.Gateway))),
    new If(
      new Not(new SafeAtHome),
      new Build(RequestAtLeast(2, Protoss.Gateway))),
    
    new Build(
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(1, Protoss.Observatory)),
    
    new RequireMiningBases(2),
    new Build(RequestAtLeast(3, Protoss.Gateway))
  )
}
