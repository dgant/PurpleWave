package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, OnMap}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.{BlueStorm, Hitchhiker}
import Strategery.Strategies.Protoss.{PvP2Gate1012Goon, PvP2GateDTExpand}

class PvPLateGame extends GameplanTemplate {

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush
  )
  
  override def workerPlan: Plan = new Parallel(
    new If(new SafeAtHome, new PumpWorkers(true, cap = 40)),
    new PumpWorkers(false, cap = 75))
  
  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val attackPlan: Plan = new PvPIdeas.AttackSafely
  override def archonPlan: Plan = new PvPIdeas.MeldArchonsPvP

  lazy val goGoonReaverCarrier = new Latch(
    new Or(
      new OnMap(BlueStorm, Hitchhiker),
      new And(
        new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
        new UnitsAtMost(0, Protoss.TemplarArchives))))

  lazy val goZealotTemplarArbiter = new Latch(
    new And(
      new Not(goGoonReaverCarrier),
      new UnitsAtLeast(1, Protoss.TemplarArchives)))
  
  class RoboTech extends Parallel(
    new Build(
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory)),
    new If(
      new UnitsAtMost(0, Protoss.TemplarArchives),
      new Build(Get(Protoss.RoboticsSupportBay))))
  
  class TemplarTech extends Parallel(
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives)),
    new If(
      new UnitsAtMost(0, Protoss.Observatory),
      new BuildCannonsAtNatural(2)))

  class BuildTech extends Parallel(
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)),

    new Build(Get(3, Protoss.Gateway)),
    new BuildGasPumps,

    // Shuttle speed
    new If(new UnitsAtLeast(1, Protoss.Shuttle), new UpgradeContinuously(Protoss.ShuttleSpeed)),

    new If(
      goZealotTemplarArbiter,
      new BuildCannonsAtNatural(2)),

    // TODO: Deal with Carriers
    new If(
      goZealotTemplarArbiter,
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Assimilator),
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives),
        Get(6, Protoss.Gateway)),
      new If(
        new EnemyStrategy(With.fingerprints.robo, With.fingerprints.fourGateGoon),
        new Build(
          Get(5, Protoss.Gateway),
          Get(2, Protoss.Assimilator),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.RoboticsSupportBay),
          Get(Protoss.Observatory)),
        new Build(
          Get(3, Protoss.Gateway),
          Get(2, Protoss.Assimilator),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(Protoss.RoboticsSupportBay),
          Get(5, Protoss.Gateway)))),

    new If(
      new UnitsAtLeast(6, Protoss.Zealot),
      new Build(Get(Protoss.CitadelOfAdun))),
    new UpgradeContinuously(Protoss.ZealotSpeed),
    new Build(Get(6, Protoss.Gateway)),
    new BuildGasPumps,

    new If(
      new And(
        new UnitsAtLeast(12, UnitMatchWarriors),
        goZealotTemplarArbiter),
      new PvPIdeas.ForgeUpgrades)
  )

  class ArbiterTransition extends Parallel(
    new Build(
      Get(Protoss.Stargate),
      Get(Protoss.ArbiterTribunal)),
    new If(
      new And(
        new UnitsAtLeast(1, Protoss.ArbiterTribunal),
        new GasPumpsAtLeast(5)),
      new Build(Get(2, Protoss.Stargate))),
    new Build(Get(Protoss.Stasis)))

  class CarrierTransition extends Parallel(
    new Build(Get(Protoss.Stargate)),
    new Build(
      Get(Protoss.FleetBeacon),
      Get(2, Protoss.Stargate),
      Get(Protoss.CarrierCapacity),
      Get(3, Protoss.Stargate)))

  override val buildPlans = Vector(

    // We're dead to DTs if we don't
    new If(
      new And(
        new UnitsAtMost(0, Protoss.Observatory),
        new Or(
          new Employing(PvP2GateDTExpand),
          new And(
            new Employing(PvP2Gate1012Goon),
            new Not(new EnemyStrategy(With.fingerprints.robo))))),
        new BuildCannonsAtNatural(2)),

    new If(
      new EnemyDarkTemplarLikely,
      new BuildCannonsAtBases(1)),

    // Dragoon Range?
    new If(
      new Or(
        new UnitsAtLeast(3, Protoss.Dragoon),
        new And(
          new UnitsAtMost(0, Protoss.CitadelOfAdun, complete = true),
          new Not(new UpgradeComplete(Protoss.ZealotSpeed)))),
        new UpgradeContinuously(Protoss.DragoonRange)),

    new PvPIdeas.TakeBase2,
    new PvPIdeas.TakeBase3,
    new PvPIdeas.TakeBase4,

    // Psi Storm?
    new If(
      new And(
        new UnitsAtLeast(1, Protoss.HighTemplar),
        new UnitsAtLeast(5, Protoss.Gateway),
        new UnitsAtLeast(8, UnitMatchWarriors),
        new Check(() => With.blackboard.keepingHighTemplar.get)),
      new Build(Get(Protoss.PsionicStorm))),

    // Observer Speed?
    new If(new EnemiesAtLeast(1, Protoss.DarkTemplar), new UpgradeContinuously(Protoss.ObserverSpeed)),

    // Air weapons
    new If(new UnitsAtLeast(1, Protoss.FleetBeacon), new UpgradeContinuously(Protoss.AirDamage)),

    // Normal army/tech
    new FlipIf(
      new Or(
        new UnitsAtLeast(30, UnitMatchWarriors),
        new And(new UnitsAtLeast(20, UnitMatchWarriors), new SafeAtHome)),
      new PvPIdeas.TrainArmy,
      new BuildTech),

    new If(
      new Not(new PvPIdeas.PvPSafeToMoveOut),
      new Build(Get(8, Protoss.Gateway))),

    new Trigger(new UnitsAtLeast(6, Protoss.Gateway, complete = true)),
    new RequireBases(3),

    // Crazy Shuttle defense
    new If(new EnemiesAtLeast(3, Protoss.Shuttle), new Build(Get(Protoss.Stargate), Get(Protoss.Corsair))),

    new Build(Get(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(Get(12, Protoss.Gateway)),
    new BuildCannonsAtExpansions(1),
    new RequireMiningBases(4),

    // Arbiter/Carrier transitions
    new If(
      new Or(goGoonReaverCarrier, new UnitsAtLeast(8, Protoss.Arbiter)),
      new CarrierTransition),
    new If(
      new Or(goZealotTemplarArbiter, new UnitsAtLeast(8, Protoss.Carrier)),
      new ArbiterTransition),

    new Build(Get(14, Protoss.Gateway)),
    new FlipIf(
      new PvPIdeas.PvPSafeToMoveOut,
      new Build(Get(22, Protoss.Gateway)),
      new RequireMiningBases(5)),
  )
}
