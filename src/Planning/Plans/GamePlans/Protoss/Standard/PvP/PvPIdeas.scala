package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel, _}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, MeldArchons}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen4GateGoon

object PvPIdeas {
  
  class PlaceShieldBatteryAtNexus extends ProposePlacement {
    override lazy val blueprints = Vector(new Blueprint(this, building = Some(Protoss.ShieldBattery), placement = Some(PlacementProfiles.hugTownHall)))
  }

  class StealGasSelectively extends If(new Not(new EnemyStrategy(With.fingerprints.twoGate)), new StealGas)
  
  class EnemyCarriersOnly extends And(
    new EnemyCarriers,
    new EnemiesAtMost(6, UnitMatchAnd(UnitMatchWarriors,  UnitMatchNot(UnitMatchMobileFlying))))
  
  class AttackWithDarkTemplar extends If(
    new Or(
      new EnemyUnitsNone(Protoss.Observer),
      new EnemyBasesAtLeast(3)),
    new Attack(Protoss.DarkTemplar))

  class PvPSafeToMoveOut extends Or(
    new BasesAtLeast(3),
    new And(
      // Are we safe against Dark Templar?
      new Or(
        new UnitsAtLeast(2, Protoss.Observer, complete = true),
        new Not(new EnemyHasShown(Protoss.DarkTemplar))),
      // Are we obligated to (or want to) move out?
      new Or(
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
        new EnemyStrategy(With.fingerprints.cannonRush),
        new Employing(PvPOpen4GateGoon),
        new MiningBasesAtLeast(3),
        new EnemyBasesAtLeast(3),
        new SafeToMoveOut),
      // Can our army contend with theirs?
      new Or(
        new UnitsAtLeast(1, UnitMatchAnd(UnitMatchWarriors, UnitMatchNot(Protoss.Dragoon))),
        new UpgradeComplete(Protoss.DragoonRange),
        new Not(new EnemyHasUpgrade(Protoss.DragoonRange))),
      // Don't mess with 4-Gates
      new Or(
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
        new UnitsAtLeast(4, Protoss.Gateway, complete = true),
        new And(
          new UnitsAtLeast(1, Protoss.Reaver, complete = true),
          new UnitsAtLeast(1, Protoss.Shuttle, complete = true)),
        new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)))))
  { override def isComplete: Boolean = super.isComplete } // Easier debugging

  class AttackSafely extends If(new PvPIdeas.PvPSafeToMoveOut, new Attack)

  class ReactToCannonRush extends If(
    new EnemyStrategy(With.fingerprints.cannonRush),
    new Parallel(
      new RequireSufficientSupply,
      new PumpWorkers,
      new Pump(Protoss.Reaver, 2),
      new PumpDragoonsAndZealots,
      new Build(
        Get(Protoss.Gateway),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay))))

  class ReactToDarkTemplarEmergencies extends Parallel(new ReactToDarkTemplarExisting, new ReactToDarkTemplarPossible)
  class ReactToDarkTemplarPossible extends If(
    new EnemyDarkTemplarLikely,
    new Parallel(
      new If(
        new UnitsAtMost(0, Protoss.Observatory),
        new Build(Get(Protoss.Forge))),
      new If(
        new And(
          new UnitsAtMost(0, Protoss.Observer, complete = true),
          new UnitsAtLeast(1, Protoss.Forge)),
        new BuildCannonsAtBases(1)),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(Protoss.Observer))))

  class ReactToDarkTemplarExisting extends If(
    new EnemyDarkTemplarExists,
    new Parallel(
      new If(
        new UnitsAtMost(0, Protoss.Observer, complete = true),
        new BuildCannonsAtBases(2)),
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory)),
      new Pump(Protoss.Observer, 3)))

  class ReactToTwoGate extends If(
    new And(
      new EnemyStrategy(With.fingerprints.twoGate),
      new UnitsAtMost(0, Protoss.Forge),
      new Or(
        new UnitsAtMost(1, Protoss.Gateway, complete = true),
        new Not(new SafeAtHome))),
    new Parallel(
      new If(
        new UnitsAtMost(7, UnitMatchWarriors),
        new Parallel(
          new RequireSufficientSupply,
          new TrainArmy)),
      new UpgradeContinuously(Protoss.DragoonRange),
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new Build(
          Get(2, Protoss.Gateway),
          Get(1, Protoss.ShieldBattery)))))

  class ReactToArbiters extends If(
    new Or(
      new EnemiesAtLeast(1, Protoss.Arbiter),
      new EnemiesAtLeast(1, Protoss.ArbiterTribunal)),
    new Parallel(
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory)),
    new Pump(Protoss.Observer, 2)))

  class ReactToProxyGateways extends If(
    new EnemyStrategy(With.fingerprints.proxyGateway),
    new Parallel(
      new RequireSufficientSupply,
      new Pump(Protoss.Probe, 9),
      new Build(Get(Protoss.Gateway)),
      new TrainArmy,
      new Build(Get(2, Protoss.Gateway)),
      new Pump(Protoss.Probe, 12),
      new Build(Get(Protoss.ShieldBattery)),
      new Pump(Protoss.Probe, 21),
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(3, Protoss.Gateway),
        Get(Protoss.DragoonRange),
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives))))

  class ReactToFFE extends If(
    new Or(
      new And(
        new EnemyStrategy(With.fingerprints.forgeFe),
        new UnitsAtLeast(1, Protoss.Gateway)),
      new And(
        new EnemyStrategy(With.fingerprints.gatewayFe),
        new UnitsAtLeast(2, Protoss.Gateway)),
      new And(
        new Not(new EnemyStrategy(With.fingerprints.cannonRush)),
        new EnemiesAtLeast(1, Protoss.PhotonCannon),
        new UnitsAtLeast(1, Protoss.Gateway))),
    new RequireMiningBases(2))

  class TakeBase2 extends If(
    new Or(
      new UnitsAtLeast(2, Protoss.Reaver, complete = true),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(8, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(16, UnitMatchWarriors, complete = true)),
    new Parallel(
      new Build(Get(2, Protoss.Gateway)),
      new RequireMiningBases(2)))

  class TakeBase3 extends If(
    new And(
      new Or(
        new Latch(new UnitsAtLeast(1, Protoss.Observer, complete = true)),
        new EnemyRobo),
      new Or(
        new UnitsAtLeast(40, UnitMatchWarriors),
        new And(
          new SafeAtHome,
          new Or(
            new EnemyCarriers,
            new EnemyBasesAtLeast(3))))),
    new Parallel(
      new RequireMiningBases(3),
      new Build(Get(7, Protoss.Gateway))))

  class MeldArchonsPvP extends MeldArchons(24) {
    override def minimumArchons: Int = Math.min(6, With.units.countEnemy(Protoss.Zealot) / 3)
  }

  class GetObserversIfDarkTemplarPossible extends If(
    new And(
      new EnemyBasesAtMost(1),
      new EnemiesAtMost(0,
        UnitMatchOr(
          Protoss.RoboticsFacility,
          Protoss.RoboticsSupportBay,
          Protoss.Observatory,
          Protoss.Observer,
          Protoss.Shuttle,
          Protoss.Reaver))),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory)))

  class PumpSufficientDragoons extends PumpMatchingRatio(Protoss.Dragoon, 0, 100, Seq(
    Enemy(Protoss.Carrier, 5.0),
    Enemy(Protoss.Scout, 2.0),
    Enemy(Protoss.Shuttle, 2.0),
    Friendly(Protoss.Zealot, 0.5),
    Friendly(Protoss.Archon, 3.0)))

  class PumpDragoonsAndZealots extends Parallel(
    new PumpMatchingRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Protoss.Carrier, 5.0))),
    new If(
      new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames + GameTime(10, 0)()),
      new PumpMatchingRatio(Protoss.Zealot, 3, 100, Seq(
        Enemy(Protoss.Carrier, -2.0),
        Friendly(Protoss.Dragoon, 2.0),
        Friendly(Protoss.Reaver, 4.0),
        Friendly(Protoss.Archon, -3.0)))),
    new Pump(Protoss.Dragoon),
    new Pump(Protoss.Zealot))

  class TrainDarkTemplar extends If(
    new Not(new EnemyCarriersOnly),
    new If(
      new And(
        new EnemiesAtMost(0, Protoss.PhotonCannon),
        new EnemiesAtMost(0, Protoss.Observer)),
      new Pump(Protoss.DarkTemplar, 3),
      new IfOnMiningBases(3, new Pump(Protoss.DarkTemplar, 1))))

  class TrainArmy extends Parallel(
    new Pump(Protoss.Carrier),
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
        new PumpMatchingRatio(Protoss.Reaver, 0, 4, Seq(Friendly(Protoss.Shuttle, 2.0))),
        new PumpMatchingRatio(Protoss.Shuttle, 0, 2, Seq(Friendly(Protoss.Reaver, 0.5))),
        new PumpMatchingRatio(Protoss.Dragoon, 3, 24, Seq(Friendly(Protoss.Zealot, 1.0))),
        new PumpMatchingRatio(Protoss.HighTemplar, 0, 8, Seq(Flat(-1), Enemy(Protoss.Dragoon, 0.2), Enemy(UnitMatchWarriors, 0.1))),
        new Pump(Protoss.Dragoon),
        new Pump(Protoss.Zealot)),
      // Dragoon-Reaver composition
      new Parallel(
        new PumpMatchingRatio(Protoss.Reaver, 1, 4, Seq(Friendly(Protoss.Shuttle, 3.0))),
        new PumpMatchingRatio(Protoss.Shuttle, 0, 2, Seq(Friendly(Protoss.Reaver, 1.0))),
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
    new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives)),
    new IfOnMiningBases(3, new Build(Get(2, Protoss.Forge))))
}
