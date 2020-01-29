package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP1ZealotExpand

class PvPVsForgeOld extends GameplanTemplate {

  // Bots have done a variety of nonsense behind FFE:
  // * Real FFE into macro
  // * Real FFE into Carrier rush
  // * Real FFE into DT rush
  // * Fake FFE into 3 proxy gates
  // This also needs to be robust against eg. Ximp turtling into Carriers

  override val activationCriteria: Predicate = new And(
    new Not(new Employing(PvP1ZealotExpand)),
    new EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.cannonRush),
    new UnitsAtMost(0, Protoss.CitadelOfAdun))
  override val completionCriteria: Predicate = new Latch(new And(new BasesAtLeast(2), new UnitsAtLeast(1, Protoss.Reaver)))

  override def buildOrderPlan: Plan = new Parallel(
    new PvPIdeas.CancelAirWeapons,
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe)),
    new If(
      new And(
        new MiningBasesAtMost(1),
        new EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.cannonRush),
        new Or(
          new EnemiesAtLeast(2, Protoss.PhotonCannon),
          new EnemyBasesAtLeast(2))),
      new Parallel(
        new CapGasWorkersAt(0),
        new BuildOrder(Get(13, Protoss.Probe)),
        new RequireMiningBases(2))),
    new BuildOrder(
      Get(Protoss.Gateway),
      Get(11, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe),
      Get(Protoss.CyberneticsCore)),
    new If(new And(new EnemiesAtLeast(1, Protoss.Zealot), new UnitsAtMost(0, Protoss.CyberneticsCore, complete = true)), new BuildOrder(Get(Protoss.Zealot))),
    new BuildOrder(
      Get(15, Protoss.Probe),
      Get(2, Protoss.Pylon),
      Get(17, Protoss.Probe),
      Get(Protoss.Dragoon)))

  override def attackPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Protoss.Reaver, complete = true),
    new PvPIdeas.AttackSafely)

  override def workerPlan: Plan = new PumpWorkers(oversaturate = true)

  override def buildPlans: Seq[Plan] = Seq(
    new BuildOrder(
      Get(Protoss.RoboticsFacility),
      Get(2, Protoss.Gateway),
      Get(Protoss.DragoonRange),
      Get(Protoss.RoboticsSupportBay),
      Get(Protoss.Shuttle)),
    new Pump(Protoss.Reaver),
    new If(new UnitsAtLeast(2, Protoss.Reaver, complete = true), new RequireMiningBases(2)),
    new If(new EnemiesAtLeast(3, Protoss.PhotonCannon), new RequireMiningBases(2)),
    new Pump(Protoss.Dragoon, 3),
    new BuildOrder(
      Get(Protoss.Reaver),
      Get(Protoss.Observatory)),
    new PvPIdeas.TrainArmy,
    new If(new Not(new SafeAtHome), new Build(Get(3, Protoss.Gateway))),
    new RequireMiningBases(2)
  )
}
