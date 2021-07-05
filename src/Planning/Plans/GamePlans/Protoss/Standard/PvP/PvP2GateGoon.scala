package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.{BuildCannonsAtNatural, BuildCannonsInMain}
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.MatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo2GateGoon

class PvP2GateGoon extends GameplanTemplate {

  override val activationCriteria : Predicate = new Employing(PvPRobo2GateGoon)
  override val completionCriteria : Predicate = new BasesAtLeast(2)

  override def scoutPlan: Plan = new ScoutOn(Protoss.Zealot)

  override def attackPlan: Plan = new If(
    new And(
      new Or(new EnemyBasesAtLeast(2), new UpgradeComplete(Protoss.DragoonRange)),
      new Or(new UnitsAtLeast(2, Protoss.Observer, complete = true), new Not(new EnemyStrategy(With.fingerprints.dtRush))),
      new Not(new EnemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))),
    super.attackPlan)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToFFE,
    new ScoutForCannonRush)
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.ZCoreZTwoGateGoon

  override val buildPlans = Vector(
    new If(
      new GasCapsUntouched,
      new If(
        new EnemyStrategy(With.fingerprints.dtRush),
        new CapGasAt(300),
        new Parallel(
          new CapGasAt(250),
          new If(
            new UnitsAtMost(0, Protoss.CyberneticsCore),
            new CapGasWorkersAt(0),
            new Trigger(
              new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
              new CapGasWorkersAt(1)))))),

    new If(new And(new EnemyStrategy(With.fingerprints.dtRush), new UnitsAtMost(0, Protoss.Observer)), new BuildCannonsInMain(1)),
    new If(new And(new EnemyStrategy(With.fingerprints.dtRush), new UnitsAtMost(0, Protoss.Observer), new EnemiesAtMost(0, Protoss.DarkTemplar)), new BuildCannonsAtNatural(2)),

    new Build(Get(Protoss.DragoonRange)),

    new If(
      new And(
        new UnitsAtLeast(8, MatchWarriors),
        new SafeAtHome,
        new Or(
          new Not(new EnemyStrategy(With.fingerprints.dtRush)),
          new UnitsAtLeast(1, Protoss.Observer, complete = true))),
      new RequireMiningBases(2)),

    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new Pump(Protoss.Observer, 2),
        new Pump(Protoss.Dragoon),
        new Pump(Protoss.Zealot)),
      new If(new EnemyStrategy(With.fingerprints.dtRush), new BuildOrder(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer)))),

    new If(
      new EnemyStrategy(With.fingerprints.dtRush),
      new Build(Get(4, Protoss.Gateway)),
      new RequireMiningBases(2)),

    new PumpWorkers(oversaturate = true)
  )
}
