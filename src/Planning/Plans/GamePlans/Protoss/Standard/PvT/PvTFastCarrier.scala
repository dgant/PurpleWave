package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Economy.MineralsAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Never
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBio}
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.MapGroups
import Strategery.Strategies.Protoss._

class PvTFastCarrier extends GameplanModeTemplate {

  class CanProxy extends Check(() => ! With.strategy.map.exists(MapGroups.badForProxying.contains))
  class CanGoCarriers extends Check(() => 4 * Math.max(4, With.units.countOurs(Protoss.Carrier)) > With.units.countEnemy(Terran.Goliath))

  override val activationCriteria     = new Employing(PvTFastCarrier)
  override val completionCriteria     = new Never //new Latch(new UnitsAtLeast(1, Protoss.FleetBeacon))
  override val defaultWorkerPlan      = new PumpWorkers(oversaturate = true)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  override def defaultScoutPlan: Plan = new If(new CanProxy, new ScoutOn(Protoss.Gateway), new ScoutOn(Protoss.Pylon))
  override val removeMineralBlocksAt  = 24

  private val buildOrderProxyGate = Vector(
    Get(8,  Protoss.Probe),
    Get(1,  Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(1,  Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(1,  Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(1,  Protoss.Zealot),
    Get(14, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(15, Protoss.Probe),
    Get(1,  Protoss.CyberneticsCore),
    Get(18, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(1,  Protoss.Dragoon),
    Get(21, Protoss.Probe),
    Get(2,  Protoss.Nexus))

  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.Opening28Nexus

  override def buildPlans: Seq[Plan] = Vector(
    /*
    new Trigger(
      new UnitsAtLeast(1, Protoss.Gateway),
      initialBefore = new If(
        new CanProxy,
        new PlaceGatewaysProxied(1, () => ProxyPlanner.proxyAutomaticSneaky),
        new PlaceGatewaysProxied(1, () => Some(With.geography.ourNatural.zone), allowBlockingBase = false))),
        */
    new EjectScout,
    new RequireMiningBases(2),
    new If(new EnemyBasesAtLeast(2), new RequireMiningBases(3)),
    new Trigger(
      new MiningBasesAtLeast(3),
      initialBefore = new If(
        new MineralsAtMost(400),
        new Trigger(
          new And(
            new UpgradeStarted(Protoss.DragoonRange),
            new UnitsAtLeast(1, Protoss.Dragoon)),
          initialBefore = new CapGasWorkersAt(3),
          initialAfter = new CapGasWorkersAt(1)))),
    new If(
      new UnitsAtLeast(6, Protoss.Gateway),
      new Parallel(
        new Build(Get(1, Protoss.CitadelOfAdun)),
        new UpgradeContinuously(Protoss.ZealotSpeed))),

    new If(
      new CanGoCarriers,
      new Parallel(
        new Trigger(
          new UnitsAtLeast(3, Protoss.Carrier),
          new If(
            new UnitsAtLeast(2, Protoss.CyberneticsCore),
            new Parallel(
              new UpgradeContinuously(Protoss.AirArmor),
              new UpgradeContinuously(Protoss.AirDamage)),
            new If(
              new And(
                new Not(new UpgradeComplete(Protoss.AirArmor, 3)),
                new Or(
                  new UpgradeComplete(Protoss.AirDamage, 3),
                  new EnemyBio)),
              new UpgradeContinuously(Protoss.AirArmor),
              new UpgradeContinuously(Protoss.AirDamage)))),
        new If(
          new Or(
            new UnitsAtLeast(1, Protoss.Carrier, complete = true),
            new UnitsAtLeast(3, Protoss.Carrier)),
          new UpgradeContinuously(Protoss.CarrierCapacity)),
      new Pump(Protoss.Carrier)),
      new Parallel(
        new If(
          new UnitsAtLeast(1, Protoss.HighTemplar),
          new Build(Get(Protoss.PsionicStorm))),
        new Pump(Protoss.HighTemplar, maximumConcurrentlyRatio = 0.25),
        new UpgradeContinuously(Protoss.GroundDamage))),

    new Trigger(
      new UnitsAtLeast(3, Protoss.Carrier),
      new BuildCannonsAtExpansions(1)),
    new Pump(Protoss.Dragoon, 6),
    new If(
      new UpgradeStarted(Protoss.ZealotSpeed),
      new Pump(Protoss.Zealot)),
    new Pump(Protoss.Dragoon),
    new RequireMiningBases(3),
    new BuildGasPumps,
    new Build(
      Get(2, Protoss.Gateway),
      Get(1, Protoss.Stargate),
      Get(1, Protoss.FleetBeacon),
      Get(3, Protoss.Stargate),
      Get(3, Protoss.Gateway)),
    new RequireMiningBases(4),
    new BuildCannonsAtNatural(1),
    new If(
      new CanGoCarriers,
      new Parallel(
        new Build(
          Get(2, Protoss.CyberneticsCore),
          Get(8, Protoss.Gateway)),
        new UpgradeContinuously(Protoss.Shields),
      new Build(
        Get(Protoss.TemplarArchives),
        Get(12, Protoss.Gateway)))),
    new RequireMiningBases(5),
    new If(
      new CanGoCarriers,
      new Build(Get(4, Protoss.Stargate)),
      new Build(Get(20, Protoss.Gateway))),
    new RequireMiningBases(6),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      Get(Protoss.PsionicStorm),
      Get(20, Protoss.Gateway)),
    new RequireMiningBases(8)
  )
}
