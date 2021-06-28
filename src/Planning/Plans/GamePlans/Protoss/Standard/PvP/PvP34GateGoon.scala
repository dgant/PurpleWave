package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyRecentStrategy, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP3GateGoon, PvP4GateGoon}

class PvP34GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP3GateGoon, PvP4GateGoon)
  override val completionCriteria : Predicate = new Latch(new MiningBasesAtLeast(2))

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon,         placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 10.0)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 12.0)),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 12.0)),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 12.0)),
    new Blueprint(Protoss.Pylon,         placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon, requireZone = Some(With.geography.ourNatural.zone))) // If we need emergency cannons in the natural, this is the Pylon we need done

  override def attackPlan: Plan = new Trigger(
    new Or(
      new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
      new And(
        new Latch(new UnitsAtLeast(5, Protoss.Dragoon, complete = true)),
        new Or(
          new Employing(PvP4GateGoon),
          new EnemyStrategy(With.fingerprints.dtRush, With.fingerprints.robo, With.fingerprints.twoGate, With.fingerprints.gasSteal),
          new EnemyBasesAtLeast(2)))),
    new PvPIdeas.AttackSafely)
  
  val oneGateCoreLogic = new PvP1GateCoreLogic(allowZealotBeforeCore = false)

  override def emergencyPlans: Seq[Plan] = oneGateCoreLogic.emergencyPlans

  override def buildOrderPlan: Plan = new (oneGateCoreLogic.BuildOrderPlan)

  override val buildPlans = Vector(

    new oneGateCoreLogic.WriteStatuses,

    new If(
      new GasCapsUntouched,
      new Parallel(
        new CapGasAt(200),
        new If(
          new UnitsAtMost(0, Protoss.CyberneticsCore),
          new CapGasWorkersAt(1),
          new If(
            new Not(new UpgradeStarted(Protoss.DragoonRange)),
            new CapGasWorkersAt(2),
            new If(
              new Employing(PvP3GateGoon),
              new If(new UnitsAtMost(2, Protoss.Gateway), new CapGasWorkersAt(1)),
              new If(new UnitsAtMost(3, Protoss.Gateway), new CapGasWorkersAt(1))))))),

    new BuildOrder(Get(Protoss.DragoonRange),  Get(2, Protoss.Dragoon)),
    new If(
      new Employing(PvP3GateGoon),
      new BuildOrder(Get(3, Protoss.Gateway), Get(8, Protoss.Dragoon)),
      new BuildOrder(Get(4, Protoss.Gateway), Get(10, Protoss.Dragoon))),

    new If(
      new Or( // All smells of DT risk
        new EnemyRecentStrategy(With.fingerprints.dtRush),
        new EnemiesAtLeast(1, Protoss.Forge),
        new EnemiesAtLeast(1, Protoss.PhotonCannon)),
      new Build(Get(Protoss.Forge))),

    new If(
      new Or(
        new And(
          new Employing(PvP3GateGoon),
          new UnitsAtLeast(3, Protoss.Gateway, complete = true)),
        new And(
          new UnitsAtLeast(16, Protoss.Dragoon),
          new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)))),
      new RequireMiningBases(2)),

    new Pump(Protoss.Dragoon),
    new Build(Get(4, Protoss.Gateway)),
    new PumpWorkers(oversaturate = true)
  )
}
