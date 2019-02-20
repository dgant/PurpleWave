package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP2Gate1012Goon, PvP2GateDTExpand, PvPLateGameArbiter, PvPLateGameCarrier}

class PvPLateGame extends GameplanTemplate {

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush
  )
  
  override def workerPlan: Plan = new Parallel(
    new If(new SafeAtHome, new PumpWorkers(true, cap = 44)),
    new PumpWorkers(false, cap = 75))
  
  override def priorityAttackPlan   : Plan = new PvPIdeas.AttackWithDarkTemplar
  override val attackPlan    : Plan = new PvPIdeas.AttackSafely
  override def archonPlan    : Plan = new PvPIdeas.MeldArchonsPvP
  
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

    new If(
      new Not(new EnemyCarriers),
      new UpgradeContinuously(Protoss.ZealotSpeed)),

    new Build(Get(3, Protoss.Gateway)),
    new BuildGasPumps,

    // Shuttle speed
    new If(new UnitsAtLeast(1, Protoss.Shuttle), new UpgradeContinuously(Protoss.ShuttleSpeed)),

    // Robo or Templar tech (or both?)
    new FlipIf(
      new Latch(
        new Or(
          new UnitsAtLeast(1, Protoss.CitadelOfAdun),
          new UnitsAtLeast(1, Protoss.TemplarArchives),
          new And(
            new UnitsAtLeast(1, Protoss.PhotonCannon),
            new UnitsAtLeast(8, Protoss.Zealot)))),
      new Parallel(
        new RoboTech,
        new Build(Get(5, Protoss.Gateway))),
      new Parallel(
        new Build(Get(5, Protoss.Gateway)),
        new TemplarTech)),

    new Build(Get(6, Protoss.Gateway)),

    new If(
      new Or(
        new GasPumpsAtLeast(3),
        new And(
          new GasPumpsAtLeast(2),
          new UnitsAtLeast(15, UnitMatchWarriors))),
        new OnGasPumps(3, new PvPIdeas.ForgeUpgrades)),

    new OnGasPumps(4,
      new If(
        new UnitsAtLeast(2, Protoss.HighTemplar),
        new UpgradeContinuously(Protoss.HighTemplarEnergy))))

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
    new UpgradeContinuously(Protoss.AirDamage),
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

    // Psi Storm?
    new If(
      new And(
        new UnitsAtLeast(1, Protoss.TemplarArchives),
        new UnitsAtLeast(5, Protoss.Gateway),
        new UnitsAtLeast(8, UnitMatchWarriors),
        new Check(() => With.blackboard.keepingHighTemplar.get)),
      new Build(Get(Protoss.PsionicStorm))),

    // Observer Speed?
    new If(new EnemiesAtLeast(1, Protoss.DarkTemplar), new UpgradeContinuously(Protoss.ObserverSpeed)),

    // Normal army/tech
    //
    new FlipIf(
      new Or(
        new UnitsAtLeast(40, UnitMatchWarriors),
        new And(new UnitsAtLeast(25, UnitMatchWarriors), new SafeAtHome)),
      new PvPIdeas.TrainArmy,
      new BuildTech),

    new If(
      new Not(new PvPIdeas.PvPSafeToMoveOut),
      new Build(Get(8, Protoss.Gateway))),
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
      new Or(new Employing(PvPLateGameCarrier), new UnitsAtLeast(8, Protoss.Arbiter)),
      new CarrierTransition),
    new If(
      new Or(new Employing(PvPLateGameArbiter), new UnitsAtLeast(8, Protoss.Carrier)),
      new ArbiterTransition),

    new Build(Get(14, Protoss.Gateway)),
    new FlipIf(
      new PvPIdeas.PvPSafeToMoveOut,
      new Build(Get(22, Protoss.Gateway)),
      new RequireMiningBases(5)),
  )
}
