package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.Get
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvP2GateDT
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.BuildCannonsAtNatural
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
  override def initialScoutPlan: Plan = new ScoutOn(Protoss.Pylon)

  override def attackPlan: Plan = new If(
    new EnemyStrategy(With.fingerprints.twelveHatch),
    new ConsiderAttacking)

  override def blueprints: Vector[Blueprint] = new PvP2GateDT().blueprints
  override def buildOrderPlan = new If(
    new EnemyIsZerg,
    new BuildOrder(ProtossBuilds.ZCoreZ: _*),
    new BuildOrder(
      // Taken lazily from the PvP 2-Gate DT build order
      // http://wiki.teamliquid.net/starcraft/2_Gateway_Dark_Templar_(vs._Protoss)
      Get(8,   Protoss.Probe),
      Get(Protoss.Pylon),                 // 8
      Get(10,  Protoss.Probe),
      Get(Protoss.Gateway),               // 10
      Get(12,  Protoss.Probe),
      Get(Protoss.Assimilator),           // 12
      Get(13,  Protoss.Probe),
      Get(Protoss.Zealot),                // 13
      Get(14,  Protoss.Probe),
      Get(2,   Protoss.Pylon),            // 16
      Get(15,  Protoss.Probe),
      Get(Protoss.CyberneticsCore),       // 17
      Get(16,  Protoss.Probe),
      Get(2,   Protoss.Zealot),           // 18 = 16 + Z
      Get(17,  Protoss.Probe),
      Get(3,   Protoss.Pylon),            // 21 = 17 + ZZ
      Get(18,  Protoss.Probe),
      Get(Protoss.Dragoon),               // 22 = 18 + ZZ
      Get(Protoss.CitadelOfAdun),         // 24
      Get(20,  Protoss.Probe),
      Get(2,   Protoss.Dragoon),          // 26
      Get(2,   Protoss.Gateway),          // 28
      Get(21,  Protoss.Probe),
      Get(Protoss.TemplarArchives),
      // Classic build gets 2 Zealots -- can we fit in? do we want to?
      Get(22,  Protoss.Probe),
      Get(4,   Protoss.Pylon),
      Get(23,  Protoss.Probe),
      Get(2,   Protoss.DarkTemplar),
      Get(24,  Protoss.Probe),
      Get(Protoss.Forge),
      Get(25,  Protoss.Probe),
      Get(5,   Protoss.Pylon)))

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
