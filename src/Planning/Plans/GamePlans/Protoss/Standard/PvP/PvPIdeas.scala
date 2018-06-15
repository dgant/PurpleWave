package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.UnitMatchers._
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers, RequireSufficientSupply, UpgradeContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, MeldArchons}
import Planning.Predicates.Economy.GasAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, WeAreBeingProxied}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen4GateGoon

object PvPIdeas {
  
  class PlaceShieldBatteryAtNexus extends ProposePlacement {
    override lazy val blueprints = Vector(new Blueprint(this, building = Some(Protoss.ShieldBattery), placement = Some(PlacementProfiles.hugTownHall)))
  }
  
  class EnemyCarriersOnly extends And(
    new EnemyCarriers,
    new EnemiesAtMost(6, UnitMatchAnd(UnitMatchWarriors,  UnitMatchNot(UnitMatchMobileFlying))))
  
  class AttackWithDarkTemplar extends If(
    new Or(
      new EnemyUnitsNone(Protoss.Observer),
      new EnemyBasesAtLeast(3)),
    new Attack(Protoss.DarkTemplar))
  
  class AttackSafely extends If(
    new And(
      new Or(
        new UnitsAtLeast(1, Protoss.Observer, complete = true),
        new Not(new EnemyDarkTemplarExists),
        new EnemiesAtMost(0, Protoss.Arbiter)),
      new Or(
        new EnemyStrategy(With.fingerprints.cannonRush),
        new Employing(PvPOpen4GateGoon),
        new SafeToMoveOut,
        new MiningBasesAtLeast(3),
        new EnemyBasesAtLeast(3)),
      new Or(
        new Not(new EnemyStrategy(With.fingerprints.twoGate)),
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true))),
    new Attack)
  
  class ReactToCannonRush extends If(
    new EnemyStrategy(With.fingerprints.cannonRush),
    new Parallel(
      new RequireSufficientSupply,
      new PumpWorkers,
      new Pump(Protoss.Reaver, 2),
      new TrainDragoonsOrZealots,
      new Build(
        Get(1, Protoss.Gateway),
        Get(1, Protoss.CyberneticsCore),
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.RoboticsSupportBay))))
  
  class ReactToDarkTemplarEmergencies extends Parallel(new ReactToDarkTemplarExisting, new ReactToDarkTemplarPossible)
  class ReactToDarkTemplarPossible extends If(
    new EnemyDarkTemplarPossible,
    new Parallel(
      new If(
        new UnitsAtMost(0, Protoss.Observatory),
        new BuildCannonsAtBases(1)),
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory),
        Get(1, Protoss.Observer))))
  
  class ReactToDarkTemplarExisting extends If(
    new EnemyDarkTemplarExists,
    new Parallel(
      new If(
        new UnitsAtMost(0, Protoss.Observatory),
        new BuildCannonsAtBases(1)),
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
      new PlaceShieldBatteryAtNexus,
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
  
  class ReactToFFE extends If(
    new And(
      new Not(new WeAreBeingProxied),
      new EnemiesAtLeast(1, Protoss.PhotonCannon),
      new SafeAtHome),
    new RequireMiningBases(2))
  
  class ReactToExpansion extends If(
    new And(
      new EnemyBasesAtLeast(2),
      new MiningBasesAtMost(1)),
    new Trigger(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Assimilator)),
      // Match if it we're already on Dragoon tech
      new RequireMiningBases(2),
      // Otherwise, let's go all in with Zealots
      new FlipIf(
        new SafeAtHome,
        new Parallel(
          new Pump(Protoss.Probe, 19),
          new Build(Get(4, Protoss.Gateway))),
        new Parallel(
          new PvPIdeas.TrainDragoonsOrZealots,
          new UpgradeContinuously(Protoss.DragoonRange)))))
  
  class TakeBase2 extends If(
    new Or(
      new UnitsAtLeast(2, Protoss.Reaver, complete = true),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(8, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(16, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(2))
  
  class TakeBase3 extends If(
    new Or(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new And(
        new SafeAtHome,
        new Or(
          new EnemyCarriers,
          new EnemyBasesAtLeast(3)))),
    new RequireMiningBases(2))
  
  class MeldArchonsPvP extends MeldArchons(49) {
    override def minimumArchons: Int = Math.min(8, With.units.countEnemy(Protoss.Zealot) / 3)
    templar.unitMatcher.set(UnitMatchAnd(Protoss.HighTemplar, UnitMatchEnergyAtMost(75)))
  }
  
  class TrainDragoonsOrZealots extends If(
    new And(
      new Not(new EnemyCarriersOnly),
      new Or(
        new UnitsAtMost(0, Protoss.CyberneticsCore,  complete = true),
        new UnitsAtMost(0, Protoss.Assimilator,      complete = true),
        new GasAtMost(30),
        new And(
          new GasAtMost(100),
          new Check(() => With.self.minerals > With.self.gas * 5)),
        new And(
          new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames),
          new Or(
            new UnitsAtLeast(12, Protoss.Dragoon),
            new Check(() => With.self.minerals > With.self.gas * 3))))),
    new Pump(Protoss.Zealot),
    new Pump(Protoss.Dragoon))
    
  class TrainDarkTemplar extends If(
    new And(
      new EnemiesAtMost(0, Protoss.PhotonCannon),
      new EnemiesAtMost(0, Protoss.Observer)),
    new Pump(Protoss.DarkTemplar, 3),
    new Pump(Protoss.DarkTemplar, 1))
    
  class TrainArmy extends Parallel(
    new Pump(Protoss.Carrier),
    new If(
      new And(
        new Not(new EnemyCarriersOnly),
        new UnitsAtMost(0, Protoss.PhotonCannon)),
      new Pump(Protoss.Observer, 1)),
    new If(
      new Not(new EnemyCarriersOnly),
      new TrainDarkTemplar),
    new Pump(Protoss.Arbiter),
    new If(
      new And(
        new Not(new EnemyCarriersOnly),
        new UnitsAtMost(0, Protoss.TemplarArchives)),
      new Pump(Protoss.Reaver, 2)),
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new Pump(Protoss.HighTemplar, 6, 2)),
    new TrainDragoonsOrZealots,
    new Pump(Protoss.Observer, 2)
  )
}
