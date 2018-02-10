package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchCustom, UnitMatchWarriors}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Information.{Employing, SafeToAttack}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasPumps, _}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT._

object PvTIdeas {
  
  class BuildSecondGasIfWeNeedIt extends If(
    new Or(
      new Employing(PvT2BaseCarrier),
      new Employing(PvT2BaseReaverCarrier),
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
        new Employing(PvTEarly1015GateGoonExpand),
        new Employing(PvTEarly1015GateGoonDT),
        new Employing(PvTEarly1015GateGoonPressure),
        new Employing(PvTEarly4Gate),
        new Employing(PvTEarly1GateStargate),
        new Employing(PvTEarly1GateStargateTemplar),
        new IfOnMiningBases(3),
        new Not(new EnemyHasShown(Terran.Vulture)),
        new UnitsAtLeast(1, UnitMatchCustom((unit) => unit.is(Protoss.Observer) && With.framesSince(unit.frameDiscovered) > 24 * 10), complete = true))),
    new Attack)
  
  class TrainScouts extends If(
    new And(
      new EnemyUnitsAtMost(0, Terran.Goliath),
      new EnemyUnitsAtMost(6, Terran.Marine),
      new EnemyUnitsAtMost(8, Terran.MissileTurret),
      new UnitsExactly(0, Protoss.FleetBeacon),
      new UnitsExactly(0, Protoss.ArbiterTribunal),
      new Or(
        new Employing(PvTEarly1GateStargate),
        new Employing(PvTEarly1GateStargateTemplar))),
    new TrainContinuously(Protoss.Scout, 5))
  
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
  
  class TrainZealotsOrDragoons extends FlipIf(
    new Or(
      new Check(() => With.self.minerals > 600 && With.self.gas < 100),
      new And(
        new UnitsAtLeast(8, Protoss.Dragoon),
        new UpgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.ZealotSpeed.upgradeTime.head._2))),
    new TrainContinuously(Protoss.Dragoon),
    new TrainContinuously(Protoss.Zealot, 30, 6))
  
  class TrainArbiters extends If(
    new UnitsAtLeast(40, UnitMatchWarriors),
    new TrainContinuously(Protoss.Arbiter, 3),
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new TrainContinuously(Protoss.Arbiter, 2),
      new TrainContinuously(Protoss.Arbiter, 10)))
  
  private class TrainObserversScalingWithArmy extends If(
    new UnitsAtLeast(1, UnitMatchWarriors),
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new If(
        new UnitsAtLeast(24, UnitMatchWarriors),
        new TrainContinuously(Protoss.Observer, 3)),
      new TrainContinuously(Protoss.Observer, 2)),
    new TrainContinuously(Protoss.Observer, 1))
  
  class TrainObservers extends If(
    new EnemyHasShownWraithCloak,
    new TrainObserversScalingWithArmy,
    new If(
      new EnemyHasShown(Terran.SpiderMine),
      new TrainObserversScalingWithArmy,
      new TrainContinuously(Protoss.Observer, 1)))
  
  class TrainHighTemplar extends OnGasPumps(3,
    new If(
      new Or(
        new UnitsAtLeast(20, UnitMatchWarriors),
        new EnemyBio),
      new TrainContinuously(Protoss.HighTemplar, 6, 2),
      new TrainContinuously(Protoss.HighTemplar, 6, 1)))
    
  class TrainArmy extends Parallel(
    new TrainObservers,
    new TrainArbiters,
    new TrainContinuously(Protoss.Reaver, 3),
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

