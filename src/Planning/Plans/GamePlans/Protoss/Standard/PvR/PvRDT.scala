package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvP2GateDarkTemplar
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy._
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvRDT

class PvRDT extends GameplanTemplate {
  override val activationCriteria: Predicate = new Employing(PvRDT)
  override val completionCriteria: Predicate = new Or(
    new EnemyIsProtoss, // Just use the default build
    new EnemyIsTerran, // Just use the default build
    new UnitsAtLeast(2, Protoss.PhotonCannon))

  override val workerPlan = NoPlan()
  override def scoutPlan: Plan = new ScoutOn(Protoss.Pylon)

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)

  override def attackPlan: Plan = new If(
    new EnemyStrategy(With.fingerprints.twelveHatch),
    new ConsiderAttacking)

  override def blueprints: Vector[Blueprint] = new PvP2GateDarkTemplar().blueprints
  override def buildOrderPlan = new If(
    new EnemyIsZerg,
    new BuildOrder(ProtossBuilds.ZCoreZ: _*),
    new BuildOrder(new PvP2GateDarkTemplar().buildOrder: _*))

  override def buildPlans: Seq[Plan] = Seq(
    new DefendFightersAgainstRush,
    new Trigger(
      new UnitsAtLeast(1, Protoss.CitadelOfAdun),
      initialBefore = new CapGasWorkersAt(2)),
    new CapGasAt(300),
    new If(
      new EnemyIsZerg,
      new Parallel(
        new Pump(Protoss.DarkTemplar),
        new PumpWorkers,
        new Pump(Protoss.Zealot, 5),
        new If(
          new EnemyStrategy(With.fingerprints.fourPool),
          new Build(Get(2, Protoss.Gateway))),
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives),
          Get(2, Protoss.Gateway),
          Get(Protoss.Forge)))),
    new BuildCannonsAtNatural(1),
    new RequireMiningBases(2),
    new BuildCannonsAtNatural(2),
    new If(
      new Or(
        new EnemyIsZerg,
        new EnemyIsProtoss),
      new Parallel(
        new Pump(Protoss.DarkTemplar),
        new Pump(Protoss.Zealot),
        new Build(Get(3, Protoss.Gateway)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.ZealotSpeed))),
  )
}
