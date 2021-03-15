package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{If, Parallel, _}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.{CancelIncomplete, CancelOrders}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildTowersAtBases, MeldArchons}
import Planning.Predicates.Compound.{And, Latch, Not, Sticky}
import Planning.Predicates.Economy.GasAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._
import Utilities.{ByOption, GameTime, Seconds}

object PvPIdeas {

  class EnemyCarriersOnly extends And(
    new EnemyCarriers,
    new EnemiesAtMost(6, MatchAnd(MatchWarriors,  MatchNot(MatchMobileFlying))))
  class UnansweredDarkTemplar extends And(
    new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
    new EnemiesAtMost(0, Protoss.Observer))

  class PvPSafeToMoveOut extends Or(
    new BasesAtLeast(3),
    new EnemyBasesAtLeast(3),
    new And(
      new EnemyStrategy(With.fingerprints.cannonRush),
      new EnemiesAtMost(0, MatchWarriors)),
    new And(
      // Are we safe against Dark Templar?
      new Or(
        new UnitsAtLeast(2, Protoss.Observer, complete = true),
        new Not(new EnemyHasShown(Protoss.DarkTemplar))),
      // Are we obligated to (or want to) move out?
      new Or(
        new And(
          new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
          new EnemiesAtMost(0, MatchOr(Protoss.Observer, Protoss.PhotonCannon))),
        new EnemyStrategy(With.fingerprints.cannonRush),
        new SafeToMoveOut),
      // Is an attack less likely to succeed than we are to get backstabbed or counter-pushed?
      // Inspired by Locutus turtling on one base and then either dropping us or four-gating behind the cannons.
      new Or(
        new Latch(
          new Or(
            new UnitsAtLeast(1, Protoss.Reaver, complete = true),
            new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
            new Employing(PvP4GateGoon),
            new And(
              new Employing(PvP3GateGoon, PvP2Gate1012Goon),
              new Not(new EnemyStrategy(With.fingerprints.fourGateGoon))),
            new And(
              new Latch(new UnitsAtLeast(5, Protoss.Gateway, complete = true)),
              new Latch(new UnitsAtLeast(20, MatchWorker, complete = true))),
            new BasesAtMost(1))),
        new EnemyBasesAtLeast(2)),
      // Can our army contend with theirs?
      new Or(
        new And(
          new UnitsAtLeast(2, Protoss.Reaver, complete = true),
          new UnitsAtLeast(1, Protoss.Shuttle, complete = true)),
        new UnitsAtLeast(1, MatchAnd(MatchWarriors, MatchNot(Protoss.Dragoon))),
        new UpgradeComplete(Protoss.DragoonRange),
        new Not(new EnemyHasUpgrade(Protoss.DragoonRange))),
      // If we expanded, wait until we can capitalize or if they've already expanded too
      new Or(
        new BasesAtMost(1),
        new EnemyBasesAtLeast(2),
        new UnansweredDarkTemplar,
        new Latch(new And(new UnitsAtLeast(2, Protoss.Reaver, complete = true), new UnitsAtLeast(1, Protoss.Shuttle, complete = true))),
        new Latch(new UnitsAtLeast(15, MatchWarriors))),
      // Don't mess with 4-Gates
      new Or(
        new Employing(PvP2Gate1012Goon, PvPRobo1012, PvP4GateGoon),
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
        new UnitsAtLeast(4, Protoss.Gateway, complete = true),
        new And(
          new UnitsAtLeast(2, Protoss.Reaver, complete = true),
          new UnitsAtLeast(1, Protoss.Shuttle, complete = true)),
        new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)))))

  class AttackSafely extends If(new PvPIdeas.PvPSafeToMoveOut, new Attack)

  class ReactToCannonRush extends If(
    new And(
      new EnemyStrategy(With.fingerprints.cannonRush),
      new FrameAtMost(GameTime(10, 0)())),
    new Parallel(
      new WriteStatus("ReactToCannonRush"),
      new BuildOrder(ProtossBuilds.ZZCore: _*),
      new RequireSufficientSupply,
      new PumpWorkers,
      new Trigger(
        new UnitsAtLeast(2, Protoss.Reaver, complete = true),
        new If(new SafeToMoveOut, new RequireMiningBases(2)),
        new CancelIncomplete(Protoss.Nexus, Protoss.CitadelOfAdun, Protoss.TemplarArchives)),
      new If(new BasesAtMost(1), new CancelIncomplete(Protoss.CitadelOfAdun, Protoss.TemplarArchives)),
      new BuildOrder(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay),
        Get(2, Protoss.Reaver),
        Get(Protoss.Observatory),
        Get(Protoss.Observer),
        Get(Protoss.DragoonRange)),
      new Pump(Protoss.Observer, 1),
      new Pump(Protoss.Reaver, 1),
      new Pump(Protoss.Observer, 2),
      new Pump(Protoss.Reaver, 2),
      new PumpWorkers(oversaturate = true),
      new PumpDragoonsAndZealots))

  // If we opened DT into Robo, we can survive but it's an emergency that requires a worker cut
  class ReactToRoboAsDT extends If(
    new And(
      new Not(new Latch(new UnitsAtLeast(5, Protoss.Gateway))),
      new Employing(PvP2GateDTExpand),
      new EnemyStrategy(With.fingerprints.robo),
      new UnitsAtLeast(25, Protoss.Probe)),
    new Parallel(
      new WriteStatus("ReactTRoboAsDT"),
      new Pump(Protoss.Dragoon),
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Assimilator))))

  // Fast proxy DT: 5:15
  // More normal timing: Closer to 6:00
  val dtArrivalTime: Int = GameTime(5, 45)()

  class ReactToDarkTemplarEmergencies extends Parallel(new ReactToDarkTemplarExisting, new ReactToDarkTemplarLikely)
  class ReactToDarkTemplarLikely extends If(
    new EnemyDarkTemplarLikely,
    new Parallel(
      new WriteStatus("ReactToDTLikely"),
      new If(
        new And(
          new UnitsAtMost(0, Protoss.RoboticsFacility),
          new FrameAtLeast(() =>
            dtArrivalTime
            - ByOption.max(With.units.ours.view.filter(Protoss.RoboticsFacility).map(_.remainingCompletionFrames)).getOrElse(Protoss.RoboticsFacility.buildFrames)
            - ByOption.max(With.units.ours.view.filter(Protoss.Observatory).map(_.remainingCompletionFrames)).getOrElse(Protoss.Observatory.buildFrames)
            - ByOption.max(With.units.ours.view.filter(Protoss.Observatory).map(_.remainingCompletionFrames)).getOrElse(Protoss.Observer.buildFrames)),
          new FrameAtLeast(() =>
            dtArrivalTime
            - Protoss.Forge.buildFrames
            - Protoss.PhotonCannon.buildFrames)),
        new Build(Get(Protoss.Forge))),
      new If(
        new And(
          new UnitsAtMost(0, Protoss.Observer, complete = true),
          new UnitsAtLeast(1, Protoss.Forge)),
        new BuildTowersAtBases(1)),
      new If(
        new UnitsAtMost(0, Protoss.Forge),
        new Build(
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(Protoss.Observer)))))

  class ReactToDarkTemplarExisting extends If(
    new EnemyHasShown(Protoss.DarkTemplar),
    new Parallel(
      new WriteStatus("ReactToDTExisting"),
      new If(
        new UnitsAtMost(0, Protoss.Observer),
        new BuildTowersAtBases(2)),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(Protoss.Observer)),
      new Pump(Protoss.Observer, 3)))

  class ReactToArbiters extends If(
    new Or(
      new EnemiesAtLeast(1, Protoss.Arbiter),
      new EnemiesAtLeast(1, Protoss.ArbiterTribunal)),
    new Parallel(
      new WriteStatus("ReactToArbiters"),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory)),
      new UpgradeContinuously(Protoss.ObserverSpeed),
      new Pump(Protoss.Observer, 3)))

  class AggressWithZealotsAgainstRangelessDragoons extends If(
    new And(
      new Latch(new UnitsAtLeast(2, Protoss.Gateway, complete = true)),
      new Latch(new UnitsAtLeast(5, Protoss.Zealot, complete = true)),
      new EnemyHasShown(Protoss.Dragoon),
      new Not(new EnemyHasUpgrade(Protoss.DragoonRange))),
    new Aggression(1.25))

  class ReactToGasSteal extends If(
    new EnemyStrategy(With.fingerprints.gasSteal),
    new Parallel(
      new WriteStatus("ReactToGasSteal"),
      new If(
        new EnemyHasShown(Protoss.PhotonCannon),
        new BuildOrder(
          Get(8, Protoss.Probe),
          Get(Protoss.Pylon),
          Get(10, Protoss.Probe),
          Get(Protoss.Gateway),
          Get(12, Protoss.Probe),
          Get(2, Protoss.Pylon),
          Get(13, Protoss.Probe),
          Get(Protoss.Zealot),
          Get(16, Protoss.Probe),
          Get(2, Protoss.Nexus))),
      new BuildOrder(ProtossBuilds.TwoGate1012: _*),
      new AggressWithZealotsAgainstRangelessDragoons,
      new PvPIdeas.ReactToDarkTemplarEmergencies,
      new Build(Get(Protoss.Assimilator)),
      new If(
        new UnitsAtMost(0, Protoss.Assimilator),
        new Parallel(
          new RequireSufficientSupply,
          new PumpWorkers(oversaturate = true),
          new Pump(Protoss.Zealot),
          new Build(
            Get(3, Protoss.Gateway),
            Get(2, Protoss.Nexus),
            Get(Protoss.Forge)),
      ))))

  val lastChanceFor2GateShieldBatteryDefense: Int = GameTime(2, 50)() - Protoss.ShieldBattery.buildFrames - Seconds(5)()

  class AntiTwoGateProxyFrom1GateCore extends Parallel(
    new WriteStatus("ReactTo2GateProxyFrom1GateCore"),
    new If(
      new FrameAtMost(lastChanceFor2GateShieldBatteryDefense),
      new CancelIncomplete(Protoss.CyberneticsCore)),
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14, Protoss.Probe),
      Get(2,  Protoss.Pylon),
      Get(15, Protoss.Probe),
      Get(2,  Protoss.Gateway),
      Get(16, Protoss.Probe),
      Get(2,  Protoss.Zealot),
      Get(Protoss.ShieldBattery),
      Get(3,  Protoss.Pylon),  // 20/25+8
      Get(18, Protoss.Probe),  // 21/25
      Get(3,  Protoss.Zealot), // 23/25
      Get(20, Protoss.Probe),  // 25/25
      Get(5,  Protoss.Zealot), // 29/34
      Get(Protoss.Assimilator),
      Get(20, Protoss.Probe),  // 29/34
      Get(Protoss.CyberneticsCore),
      Get(21, Protoss.Probe),
      Get(7, Protoss.Zealot)))

  class AntiTwoGateInBaseFrom1GateCore extends Parallel(
    new WriteStatus("ReactTo2GateInBaseFrom1GateCore"),
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14, Protoss.Probe),
      Get(2,  Protoss.Pylon),
      Get(16, Protoss.Probe),
      Get(2, Protoss.Gateway),
      Get(2,  Protoss.Zealot),
      Get(17, Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(18, Protoss.Probe),
      Get(3, Protoss.Pylon),
      Get(19, Protoss.Probe)),
    new If(
      new UnitsAtMost(0, Protoss.CyberneticsCore, complete = true),
      new Pump(Protoss.Zealot),
      new BuildOrder(Get(2, Protoss.Dragoon))),
    new BuildOrder(
      Get(21, Protoss.Probe),
      Get(4, Protoss.Dragoon),
      Get(Protoss.DragoonRange)))

  class PerformReactionTo2Gate extends Parallel(
    new WriteStatus("ReactTo2Gate"),
    new CapGasAt(0, 300),
    new If(
      new UnitsAtMost(0, Protoss.CyberneticsCore),
      new CapGasWorkersAt(0),
      new If(
        new UnitsAtMost(19, Protoss.Probe),
        new CapGasWorkersAt(1))),
    new Pump(Protoss.Probe, 8),
    new If(new UnitsAtMost(0, Protoss.CyberneticsCore), new CancelIncomplete(Protoss.Nexus)),
    new If(
      new Sticky(
        new And(
          new EnemyStrategy(With.fingerprints.proxyGateway),
          new FrameAtMost(lastChanceFor2GateShieldBatteryDefense))),
      new AntiTwoGateProxyFrom1GateCore,
      new AntiTwoGateInBaseFrom1GateCore),
    new RequireSufficientSupply,
    new Pump(Protoss.Probe, 16),
    new FlipIf(
      new UnitsAtLeast(5, MatchWarriors),
      new TrainArmy,
      new Parallel(
        new PumpWorkers,
        new UpgradeContinuously(Protoss.DragoonRange))))

  class ReactTo2Gate extends If(
    new And(
      new EnemyStrategy(With.fingerprints.twoGate),
      new FrameAtMost(GameTime(6, 0)())),
    new Parallel(
      new WriteStatus("ReactTo2Gate"),
      new PerformReactionTo2Gate))

  class ReactToProxyGateways extends If(
    new And(
      new EnemyStrategy(With.fingerprints.proxyGateway),
      new FrameAtMost(GameTime(6, 0)())),
    new Parallel(
      new WriteStatus("ReactToProxyGate"),
      new PerformReactionTo2Gate,
      new If(new UpgradeStarted(Protoss.DragoonRange), new Attack),
      new If(
        new Not(new SafeAtHome),
        new Build(
          Get(2, Protoss.Gateway),
          Get(2, Protoss.ShieldBattery)))))

  class ReactToFFE extends If(
    new Or(
      new And(
        new EnemyStrategy(With.fingerprints.forgeFe),
        new UnitsAtLeast(1, Protoss.Gateway)),
      new And(
        new EnemyStrategy(With.fingerprints.gatewayFe),
        new UnitsAtLeast(2, MatchOr(Protoss.Gateway, Protoss.CyberneticsCore))),
      new And(
        new Not(new EnemyStrategy(With.fingerprints.cannonRush)),
        new EnemiesAtLeast(1, Protoss.PhotonCannon),
        new UnitsAtLeast(1, Protoss.Gateway))),
    new Parallel(
      new WriteStatus("ReactToFFE"),
      new RequireMiningBases(2),
      new PumpWorkers,
      new If(
        new MiningBasesAtMost(1),
        new CapGasWorkersAt(0))))

  class TakeBase2 extends If(
    new Or(
      new UnitsAtLeast(2, Protoss.Reaver, complete = true),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(8, MatchWarriors, complete = true)),
      new UnitsAtLeast(16, MatchWarriors, complete = true)),
    new RequireMiningBases(2))

  class TakeBase3WithGateways extends If(
    new And(
      new Or(
        new Latch(new UnitsAtLeast(1, Protoss.Observer, complete = true)),
        new EnemyRobo),
      new Or(
        new UnitsAtLeast(15, MatchWarriors),
        new EnemiesAtLeast(6, Protoss.PhotonCannon),
        new And(
          new SafeAtHome,
          new UnitsAtLeast(5, Protoss.Gateway, complete = true),
          new Or(
            new EnemyCarriers,
            new EnemyBasesAtLeast(3))))),
    new Parallel(
      new Build(Get(5, Protoss.Gateway)),
      new RequireBases(3)))


  class MeldArchonsPvP extends MeldArchons(0) {
    override def minimumArchons: Int = Math.min(6, With.units.countEnemy(Protoss.Zealot) / 6)
  }

  class PumpSufficientDragoons extends PumpRatio(Protoss.Dragoon, 0, 100, Seq(
    Enemy(Protoss.Carrier, 5.0),
    Enemy(Protoss.Scout, 2.0),
    Enemy(Protoss.Shuttle, 2.0),
    Friendly(Protoss.Zealot, 0.5),
    Friendly(Protoss.Archon, 3.0)))

  class ZealotsAllowed extends And(
    // Zealots work in our army
    new Or(
      new GasAtMost(40),
      new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
      new UpgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.Zealot.buildFrames),
      new UnitsAtMost(0, Protoss.CyberneticsCore, complete = true)),
    // Zealots work against the enemy
    new Or(
      new EnemiesAtMost(0, Protoss.Carrier),
      new EnemiesAtLeast(7, MatchAnd(MatchWarriors, MatchNot(Protoss.Zealot)))))

  class PumpDragoonsAndZealots extends Parallel(
    new PumpRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Protoss.Carrier, 5.0))),
    new If(
      new And(new ZealotsAllowed, new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames)),
      new PumpRatio(Protoss.Zealot, 3, 100, Seq(
        Enemy(Protoss.Carrier, -8.0),
        Friendly(Protoss.Dragoon, 2.0),
        Friendly(Protoss.Reaver, 4.0),
        Friendly(Protoss.Archon, -3.0)))),
    new Pump(Protoss.Dragoon),
    new If(new ZealotsAllowed, new Pump(Protoss.Zealot)))

  class TrainDarkTemplar extends If(
    new EnemiesAtMost(0, Protoss.Observer),
    new If(
      new EnemiesAtMost(0, Protoss.PhotonCannon),
      new Pump(Protoss.DarkTemplar, 3),
      new Pump(Protoss.DarkTemplar, 2)),
    new If(new GasPumpsAtLeast(2), new Pump(Protoss.DarkTemplar, 1)))

  class TrainArmy extends Parallel(
    new Pump(Protoss.Carrier),
    new PumpRatio(Protoss.Observer, 0, 1, Seq(Enemy(Protoss.DarkTemplar, 1.0))),
    new PumpRatio(Protoss.Dragoon, 0, 80, Seq(Enemy(Protoss.Carrier, 6.0))),
    new If(
      new And(
        new Not(new EnemyCarriersOnly),
        new UnitsAtMost(0, Protoss.PhotonCannon),
        new Or(
          new SafeAtHome,
          new UnitsAtMost(0, Protoss.RoboticsSupportBay))),
      new Pump(Protoss.Observer, 1)),
    new TrainDarkTemplar,
    new Pump(Protoss.Arbiter),
    new PumpSufficientDragoons,
    new If(
      new And(
        new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames),
        new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true)),

      // Speedlot-Templar composition
      new Parallel(
        new PumpShuttleAndReavers(6, shuttleFirst = false),
        new PumpRatio(Protoss.Dragoon, 3, 24, Seq(Friendly(Protoss.Zealot, 1.5))),
        new PumpRatio(Protoss.HighTemplar, 0, 8, Seq(Flat(-1), Friendly(MatchWarriors, 1.0 / 4.0))),
        new If(new ZealotsAllowed, new Pump(Protoss.Zealot))),

      // Dragoon-Reaver composition
      new Parallel(
        new PumpShuttleAndReavers(6, shuttleFirst = false),
        new PumpDragoonsAndZealots)),

    new If(
      new Or(
        new EnemyDarkTemplarLikely,
        new BasesAtLeast(2)),
      new Pump(Protoss.Observer, 2)))

  class ForgeUpgrades extends Parallel(
    new Build(Get(Protoss.Forge)),
    new If(
      new UnitsAtMost(0, Protoss.TemplarArchives, complete = true),
      new If(
        new UpgradeComplete(Protoss.GroundDamage, 1),
        new Build(Get(Protoss.GroundArmor, 1)),
        new Build(Get(Protoss.GroundDamage, 1))),
      new If(
        new UnitsAtLeast(2, Protoss.Forge),
        new Parallel(
          new UpgradeContinuously(Protoss.GroundArmor),
          new UpgradeContinuously(Protoss.GroundDamage)),
        new If(
          new UpgradeComplete(Protoss.GroundDamage, 3),
          new UpgradeContinuously(Protoss.GroundArmor),
          new UpgradeContinuously(Protoss.GroundDamage)))),
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives)),
    new IfOnMiningBases(3, new Build(Get(2, Protoss.Forge))))

  class CancelAirWeapons extends If(
    new And(new UpgradeStarted(Protoss.AirDamage), new Not(new UpgradeComplete(Protoss.AirDamage))),
    new CancelOrders(Protoss.CyberneticsCore))
}
