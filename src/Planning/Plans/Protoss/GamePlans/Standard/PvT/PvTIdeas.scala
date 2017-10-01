package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchCustom, UnitMatchWarriors}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.{Employing, SafeToAttack}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, _}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT._

object PvTIdeas {
  
  class BuildSecondGasIfWeNeedIt extends If(
    new Or(
      new Employing(PvT2BaseCarrier),
      new Employing(PvT2BaseArbiter)),
    new BuildGasPumps)
  
  class Require2BaseTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.DragoonRange)),
    new RequireMiningBases(2),
    new BuildGasPumps)
  
  class Require3BaseTech extends Parallel(
    new Require2BaseTech,
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(4, Protoss.Gateway),
      RequestTech(Protoss.PsionicStorm),
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge)))
  
  class AttackWithDarkTemplar extends Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) }
  
  class AttackWithScouts extends Attack { attackers.get.unitMatcher.set(Protoss.Scout) }
  
  class AttackWithCarrierFleet extends Trigger(
    new UnitsAtLeast(4, Protoss.Carrier),
    initialAfter = new Attack { attackers.get.unitMatcher.set(Protoss.Carrier) })
  
  class PriorityAttacks extends Parallel(
    new AttackWithDarkTemplar,
    new AttackWithScouts,
    new AttackWithCarrierFleet)
  
  class AttackRespectingMines extends If(
    new And(
      new Or(
        new SafeToAttack,
        new UnitsAtLeast(6, UnitMatchWarriors, complete = true)),
      new Or(
        new Employing(PvTEarly1015GateGoon),
        new Employing(PvTEarly1GateStargate),
        new Employing(PvTEarlyStove),
        new OnMiningBases(3),
        new Not(new EnemyHasShown(Terran.SpiderMine)),
        new UnitsAtLeast(1, UnitMatchCustom((unit) => unit.is(Protoss.Observer) && With.framesSince(unit.frameDiscovered) > 24 * 10), complete = true))),
    new Attack)
  
  class TrainScouts extends If(
    new And(
      new UnitsExactly(0, Terran.Goliath),
      new UnitsAtMost(6, Terran.Marine),
      new UnitsExactly(0, Protoss.FleetBeacon),
      new UnitsExactly(0, Protoss.ArbiterTribunal),
      new Or(
        new Employing(PvTEarly1GateStargate),
        new Employing(PvTEarlyStove))),
    new TrainContinuously(Protoss.Scout, 3))
  
  class TrainDarkTemplar extends If(
    new And(
      new UnitsAtMost(0, Protoss.Arbiter),
      new EnemyUnitsNone(Terran.ScienceVessel),
      new EnemyUnitsNone(UnitMatchCustom((unit) => unit.is(Terran.MissileTurret) && unit.zone.owner.isNeutral))),
    new TrainContinuously(Protoss.DarkTemplar, 3),
    new TrainContinuously(Protoss.DarkTemplar, 1))
  
  private class IfCloakedThreats_Observers extends If(
    new Or(
      new EnemyHasShown(Terran.Vulture),
      new EnemyHasShown(Terran.SpiderMine),
      new EnemyHasShownWraithCloak),
    new Build(
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)))
  
  class TrainZealotsOrDragoons extends If(
    new And(
      new UpgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.ZealotSpeed.upgradeTime.head._2),
      new UnitsAtMost(12, Protoss.Zealot),
      new Check(() => With.units.ours.count(_.is(Protoss.Dragoon)) >= With.units.enemy.count(_.is(Terran.Vulture))),
      new Or(
        new UnitsAtLeast(12, Protoss.Dragoon),
        new Check(() => With.self.minerals > 600 && With.self.gas < 100))),
    new TrainContinuously(Protoss.Zealot, 30),
    new TrainContinuously(Protoss.Dragoon))
  
  class TrainArbiters extends If(
    new UnitsAtLeast(40, UnitMatchWarriors),
    new TrainContinuously(Protoss.Arbiter, 3),
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new TrainContinuously(Protoss.Arbiter, 2),
      new TrainContinuously(Protoss.Arbiter, 10)))
  
  class TrainObservers extends If(
    new EnemyHasShownWraithCloak,
    new TrainContinuously(Protoss.Observer, 3),
    new If(
      new EnemyHasShown(Terran.SpiderMine),
      new TrainContinuously(Protoss.Observer, 3),
      new TrainContinuously(Protoss.Observer, 1)))
  
  class TrainHighTemplar extends OnGasBases(3,
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new TrainContinuously(Protoss.HighTemplar, 6, 2),
      new TrainContinuously(Protoss.HighTemplar, 6, 1)))
    
  class TrainArmy extends Parallel(
    new TrainObservers,
    new TrainArbiters,
    new TrainContinuously(Protoss.Carrier),
    new TrainDarkTemplar,
    new TrainHighTemplar,
    new TrainScouts,
    new TrainZealotsOrDragoons)
  
  class GetObserversForCloakedWraiths extends If(
    new EnemyHasShownWraithCloak,
    new Parallel(
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory)),
      new PvTIdeas.TrainObservers))
}

