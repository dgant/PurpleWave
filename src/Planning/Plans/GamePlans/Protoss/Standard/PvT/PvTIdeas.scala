package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelAll
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.{GasAtLeast, GasAtMost, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBio}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchCustom, UnitMatchOr, UnitMatchSiegeTank, UnitMatchWarriors}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.{PvT1015DT, PvT1015Expand, PvTStove}

object PvTIdeas {
  
  class AttackWithDarkTemplar extends If(
    new Or(
      new EnemyUnitsNone(Protoss.Observer),
      new EnemyBasesAtLeast(3)),
    new Attack(Protoss.DarkTemplar))
  
  class AttackWithScouts extends Attack(Protoss.Scout)
  
  class AttackWithCarrierFleet extends Trigger(
    new UnitsAtLeast(4, Protoss.Carrier),
    initialAfter = new Attack(Protoss.Carrier))
  
  class PriorityAttacks extends Parallel(
    new AttackWithDarkTemplar,
    new AttackWithScouts,
    new AttackWithCarrierFleet)
  
  class AttackSafely extends If(
    new Or(
      new Not(new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113)),
      new Latch(new UnitsAtLeast(12, UnitMatchWarriors))),
    new If(
      new Or(
        new Employing(PvT1015Expand),
        new Employing(PvT1015DT),
        new Employing(PvTStove),
        new MiningBasesAtLeast(3),
        new EnemyBasesAtLeast(2),
        new EnemyBio,
        new Not(new EnemyHasShown(Terran.Vulture)),
        new UnitsAtLeast(12, UnitMatchWarriors, complete = true),
        new UnitsAtLeast(1, UnitMatchCustom((unit) => unit.is(Protoss.Observer) && With.framesSince(unit.frameDiscovered) > 24 * 10), complete = true)),
      new ConsiderAttacking))

  class ReactToBBS extends If(
    new And(
      new FrameAtMost(GameTime(10, 0)()),
      new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113)),
    new Parallel(
      new If(
        new UnitsAtMost(5, UnitMatchWarriors),
        new CancelAll(Protoss.Nexus)),
      new If(
        new UnitsAtMost(1, Protoss.Gateway),
        new CancelAll(UnitMatchOr(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus, Protoss.Stargate))),
      new RequireSufficientSupply,
      new Pump(Protoss.DarkTemplar, 3),
      new If(
        new UnitsAtLeast(3, Protoss.Dragoon),
        new Build(Get(Protoss.DragoonRange))),
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new Pump(Protoss.Dragoon),
        new If(
          new UnitsAtMost(0, Terran.Bunker, complete = true),
          new Pump(Protoss.Zealot),
          new Pump(Protoss.Zealot, 5))),
      new Build(
        Get(9, Protoss.Probe),
        Get(Protoss.Pylon),
        Get(12, Protoss.Probe),
        Get(2, Protoss.Gateway),
        Get(18, Protoss.Probe),
        Get(Protoss.ShieldBattery),
        Get(3, Protoss.Gateway)),
      new If(
        new UnitsAtLeast(1, Protoss.Dragoon),
        new UpgradeContinuously(Protoss.DragoonRange)),
      new Pump(Protoss.Probe),
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives),
        Get(3, Protoss.Gateway),
        Get(2, Protoss.Nexus))))

  class ReactTo2Fac extends If(
    new And(
      new FrameAtMost(GameTime(10, 0)()),
      new EnemyStrategy(With.fingerprints.twoFac)),
    new Parallel(
      new RequireSufficientSupply,
      new Pump(Protoss.Dragoon, 7),
      new Build(
        Get(9, Protoss.Probe),
        Get(Protoss.Gateway),
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(18, Protoss.Probe),
        Get(Protoss.DragoonRange),
        Get(2, Protoss.Gateway),
        Get(2, Protoss.Nexus)),
      new PumpWorkers,
      new Build(
        Get(3, Protoss.Gateway),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory))))
  
  class TrainMinimumDragoons extends Parallel(
    new PumpMatchingRatio(Protoss.Dragoon, 1, 3, Seq(Enemy(Terran.Vulture, 1.0), Enemy(Terran.Wraith, 1.0))),
    new PumpMatchingRatio(Protoss.Dragoon, 1, 20, Seq(Enemy(Terran.Vulture, 0.6), Enemy(Terran.Wraith, 0.5))))

  class TrainDarkTemplar extends If(
    new Or(
      new UnitsAtMost(0, UnitMatchOr(Protoss.Arbiter, Protoss.ArbiterTribunal)),
      new And(
        new UnitsAtMost(0, Protoss.Arbiter, complete = true),
        new EnemyStrategy(
          With.fingerprints.fiveRax,
          With.fingerprints.bbs,
          With.fingerprints.twoRax1113,
          With.fingerprints.twoFac,
          With.fingerprints.threeFac))),
    new If(
      new And(
        new EnemiesAtMost(8, Terran.Vulture),
        new EnemyUnitsNone(Terran.ScienceVessel),
        new EnemyUnitsNone(UnitMatchCustom((unit) => unit.is(Terran.MissileTurret) && unit.zone.owner.isNeutral))),
      new Pump(Protoss.DarkTemplar, 4)))

  private class TrainObservers extends If(
    new UnitsAtLeast(24, UnitMatchWarriors),
    new Pump(Protoss.Observer, 4),
    new If(
      new UnitsAtLeast(18, UnitMatchWarriors),
      new Pump(Protoss.Observer, 3),
      new If(
        new UnitsAtLeast(12, UnitMatchWarriors),
        new Pump(Protoss.Observer, 2),
        new If(
          new UnitsAtLeast(3, UnitMatchWarriors),
          new Pump(Protoss.Observer, 1)))))

  class TrainReavers extends Parallel(
    new PumpMatchingRatio(Protoss.Reaver, 0, 6, Seq(
      Enemy(Terran.Marine, 1.0/6.0),
      Enemy(Terran.Goliath, 1.0/6.0),
      Enemy(Terran.Vulture, 1.0/8.0))),
    new If(
      new EnemiesAtMost(0, UnitMatchOr(Terran.Wraith, Terran.Goliath)),
      new PumpMatchingRatio(Protoss.Reaver, 1, 4, Seq(Friendly(Protoss.Shuttle, 0.5)))))

  class TrainHighTemplarAgainstBio extends If(
    new EnemyBio,
    new PumpMatchingRatio(Protoss.HighTemplar, 1, 6, Seq(Enemy(Terran.Marine, 1.0/5.0))))

  class TrainScouts extends If(
    new And(
      new EnemiesAtMost(0, Terran.Goliath),
      new EnemiesAtMost(6, Terran.Marine),
      new EnemiesAtMost(8, Terran.MissileTurret),
      new UnitsExactly(0, Protoss.FleetBeacon),
      new UnitsExactly(0, Protoss.ArbiterTribunal),
      new Employing(PvTStove)),
    new Pump(Protoss.Scout, 5))

  class TrainZealotsOrDragoons extends Parallel(
    new PumpMatchingRatio(Protoss.Dragoon, 0, 24, Seq(Friendly(Protoss.Zealot, .75))),
    new If(
      new Or(
        new And(
          new MineralsAtLeast(600),
          new GasAtMost(200)),
        new UpgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.ZealotSpeed.upgradeFrames.head._2)),
      new PumpMatchingRatio(Protoss.Zealot, 0, 24, Seq(
        Enemy(UnitMatchSiegeTank, 3.0),
        Enemy(Terran.Goliath,     2.0),
        Enemy(Terran.Marine,      1.0),
        Enemy(Terran.Vulture,     -0.75)))),
    new Pump(Protoss.Dragoon),
    new If(new BasesAtLeast(3), new Pump(Protoss.Zealot)))

  class TrainArmy extends Parallel(
    new TrainDarkTemplar,
    new PumpMatchingRatio(Protoss.Shuttle, 0, 1, Seq(Friendly(Protoss.Reaver, 1.0))),
    new PumpMatchingRatio(Protoss.Shuttle, 0, 2, Seq(Friendly(Protoss.Reaver, 0.5))),
    new TrainReavers,
    new TrainObservers,
    new TrainMinimumDragoons,
    new TrainHighTemplarAgainstBio,
    new PumpMatchingRatio(Protoss.Arbiter, 0, 2, Seq(Friendly(Protoss.Carrier, 1.0 / 8.0))),
    new PumpMatchingRatio(Protoss.HighTemplar, 0, 2, Seq(Friendly(Protoss.Carrier, 1.0 / 8.0))),
    new Pump(Protoss.Carrier),
    new Pump(Protoss.Arbiter, 12),
    new If(new GasAtLeast(500), new Pump(Protoss.HighTemplar, maximumConcurrently = 4)),
    new TrainScouts,
    new TrainZealotsOrDragoons)
  
  class GetObserversForCloakedWraiths extends If(
    new EnemyHasShownWraithCloak,
    new Parallel(
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory)),
      new PvTIdeas.TrainObservers))
}

