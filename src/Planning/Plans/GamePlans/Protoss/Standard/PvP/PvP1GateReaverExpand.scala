package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.PumpWorkers
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen1GateReaverExpand

class PvP1GateReaverExpand extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(PvPOpen1GateReaverExpand)
  override val completionCriteria: Predicate = new Latch(new And(new UnitsAtLeast(2, Protoss.Nexus), new UnitsAtLeast(1, Protoss.RoboticsSupportBay)))
  
  override def defaultWorkerPlan: Plan = new PumpWorkers(true)
  override def defaultScoutPlan: Plan = new ScoutOn(Protoss.Gateway)
  override val defaultAttackPlan: Plan = new If(new EnemyStrategy(With.fingerprints.nexusFirst), new PvPIdeas.AttackSafely)
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE)

  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(
      Get(8,   Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10,  Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12,  Protoss.Probe),
      Get(2,   Protoss.Pylon),
      Get(13,  Protoss.Probe),
      Get(1,   Protoss.Zealot),
      Get(14,  Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(15,  Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(16,  Protoss.Probe),
      Get(2,   Protoss.Zealot), // Typical ZCoreZ ends here
      Get(18,  Protoss.Probe),
      Get(1,   Protoss.Dragoon),
      Get(19,  Protoss.Probe), // Delay pylon, cutting probes, to get defensive tech online
      Get(Protoss.DragoonRange),
      Get(Protoss.RoboticsFacility),
      Get(3,   Protoss.Pylon)),
    new If(
      new Not(new EnemyStrategy(With.fingerprints.nexusFirst)),
      new BuildOrder(
        Get(2,   Protoss.Gateway),
        Get(20,  Protoss.Probe),
        Get(2,   Protoss.Dragoon),
        Get(21,  Protoss.Probe)))
  )

  override def buildPlans = Vector(

    new EjectScout,

    new If(
      new Or(
        new EnemyStrategy(With.fingerprints.nexusFirst),
        new UnitsAtLeast(2, Protoss.Reaver, complete = true)),
      new RequireMiningBases(2)),

    new FlipIf(
      new Or(
        new EnemyDarkTemplarLikely,
        new EnemyHasShown(Protoss.Forge),
        new EnemyHasShown(Protoss.PhotonCannon)),
      new Build(Get(Protoss.RoboticsSupportBay)),
      new If(
        new Not(new EnemyHasShown(Protoss.RoboticsFacility)),
        new Build(Get(Protoss.Observatory)))),

    new PvPIdeas.TrainArmy,

    new If(
      new EnemyStrategy(With.fingerprints.twoGate),
      new Build(Get(Protoss.ShieldBattery))),

    new If(
      new EnemyBasesAtLeast(2),
      new RequireMiningBases(2)),

    new FlipIf(
      new SafeAtHome,
      new Build(Get(3, Protoss.Gateway)),
      new RequireMiningBases(2)),

    new Build(Get(5, Protoss.Gateway))
  )
}
