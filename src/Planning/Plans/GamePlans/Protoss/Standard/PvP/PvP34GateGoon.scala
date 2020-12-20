package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, GasCapsUntouched, Pump}
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyDarkTemplarLikely}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP3GateGoon, PvP4GateGoon}

class PvP34GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP3GateGoon, PvP4GateGoon)
  override val completionCriteria : Predicate = new MiningBasesAtLeast(2)

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

  override def priorityAttackPlan : Plan = new AttackWithDarkTemplar
  override def attackPlan: Plan = new Trigger(
    new Or(
      new EnemyStrategy(With.fingerprints.gasSteal),
      new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
      new UnitsAtLeast(5, Protoss.Dragoon, complete = true)),
    new PvPIdeas.AttackSafely)
  
  val oneGateCoreLogic = new PvP1GateCoreLogic(allowZealotBeforeCore = false)

  override def emergencyPlans: Seq[Plan] = Seq(new oneGateCoreLogic.BuildOrderPlan)

  override def buildOrderPlan: Plan = new oneGateCoreLogic.BuildOrderPlan

  override val buildPlans = Vector(

    new EjectScout,
    new oneGateCoreLogic.WriteStatuses,

    new If(
      new GasCapsUntouched,
      new Parallel(
        new If(
          new UnitsAtMost(0, Protoss.CyberneticsCore),
          new CapGasWorkersAt(1),
          new If(
            new GasAtLeast(200),
            new CapGasWorkersAt(1),
            new If(
              new UnitsAtMost(2, Protoss.Gateway),
              new CapGasWorkersAt(2)))),
        new CapGasAt(200))),

    new BuildOrder(Get(Protoss.DragoonRange),  Get(2, Protoss.Dragoon)),
    new If(
      new Employing(PvP3GateGoon),
      new BuildOrder(Get(3, Protoss.Gateway), Get(8, Protoss.Dragoon)),
      new BuildOrder(Get(4, Protoss.Gateway), Get(10, Protoss.Dragoon))),

    // Kind of cowardly, but if we can't be sure they're not going DT, get a Forge before expanding so we can get cannons in time if necessary
    new If(
      new And(
        new Not(new EnemyDarkTemplarLikely),
        new Not(new EnemyHasShownCloakedThreat),
        new EnemyStrategy(
          With.fingerprints.nexusFirst,
          With.fingerprints.twoGate,
          With.fingerprints.forgeFe,
          With.fingerprints.gatewayFe,
          With.fingerprints.dragoonRange,
          With.fingerprints.fourGateGoon,
          With.fingerprints.robo)),
      new CancelIncomplete(Protoss.Forge),
      new Build(Get(Protoss.Forge))),

    new FlipIf(
      new Or(
        new EnemyBasesAtLeast(2),
        new UnitsAtLeast(20, Protoss.Dragoon)),
      new If(
        new Or(
          new And(
            new Employing(PvP3GateGoon),
            new UnitsAtLeast(3, Protoss.Gateway, complete = true)),
          new UnitsAtLeast(14, Protoss.Dragoon)),
        new RequireMiningBases(2)),
      new Pump(Protoss.Dragoon)),

    new Build(Get(4, Protoss.Gateway))
  )
}
