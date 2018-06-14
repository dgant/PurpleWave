package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.{Latch, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.PumpWorkers
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen1GateReaverExpand

class PvP1GateReaverExpand extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(PvPOpen1GateReaverExpand)
  override val completionCriteria: Predicate = new Latch(new And(new UnitsAtLeast(2, Protoss.Nexus), new UnitsAtLeast(1, Protoss.RoboticsSupportBay)))
  
  override def defaultWorkerPlan: Plan = new PumpWorkers(true)
  override val defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToTwoGate
  )
  
  override val buildOrder = ProtossBuilds.OpeningZCoreZ
  override def buildPlans = Vector(
    
    new If(
      new UnitsAtLeast(2, Protoss.Reaver, complete = true),
      new RequireMiningBases(2)),
  
    new PvPIdeas.TrainArmy,
  
    new If(
      new EnemyStrategy(With.fingerprints.proxyGateway),
      new Build(Get(3, Protoss.Gateway))),
    new If(
      new EnemyStrategy(With.fingerprints.twoGate),
      new Build(Get(2, Protoss.Gateway))),
    
    new Build(
      Get(Protoss.DragoonRange),
      Get(1, Protoss.RoboticsFacility)),
    
    new FlipIf(
      new SafeAtHome,
      new Build(
        Get(1, Protoss.RoboticsSupportBay),
        Get(2, Protoss.Gateway),
        Get(1, Protoss.Observatory)),
      new Build(
        Get(2, Protoss.Nexus),
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory),
        Get(1, Protoss.RoboticsSupportBay)))
  )
}
