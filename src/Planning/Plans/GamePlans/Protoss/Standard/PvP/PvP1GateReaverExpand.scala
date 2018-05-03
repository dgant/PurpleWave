package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainWorkersContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen1GateReaverExpand

class PvP1GateReaverExpand extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(PvPOpen1GateReaverExpand)
  override val completionCriteria: Plan = new Latch(new And(new UnitsAtLeast(2, Protoss.Nexus), new UnitsAtLeast(1, Protoss.RoboticsSupportBay)))
  
  override def defaultWorkerPlan: Plan = new TrainWorkersContinuously(true)
  override val defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToCannonRush
  )
  
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
    
    new Build(
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.RoboticsFacility)),
    
    new FlipIf(
      new SafeAtHome,
      new Build(
        RequestAtLeast(1, Protoss.RoboticsSupportBay),
        RequestAtLeast(2, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Observatory)),
      new Build(
        RequestAtLeast(2, Protoss.Nexus),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(1, Protoss.RoboticsSupportBay)))
  )
}
