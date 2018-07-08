package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.DefendZones
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyCarriers, EnemyDarkTemplarLikely, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPLateGameArbiter, PvPLateGameCarrier}

class PvPLateGame extends GameplanModeTemplate {
  
  override val scoutExpansionsAt = 90
  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush
  )
  
  override def aggression: Double = 0.92
  
  override def defaultWorkerPlan: Plan = new If(
    new SafeAtHome,
    new PumpWorkers(true),
    new PumpWorkers(false))
  
  override def priorityAttackPlan   : Plan = new PvPIdeas.AttackWithDarkTemplar
  override def priorityDefensePlan  : Plan = new DefendZones { defenderMatcher.set(Protoss.Corsair) }
  override val defaultAttackPlan    : Plan = new PvPIdeas.AttackSafely
  override def defaultArchonPlan    : Plan = new PvPIdeas.MeldArchonsPvP
  
  class RoboTech extends Parallel(
    new Build(
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory)),
    new If(
      new EnemyHasShownCloakedThreat,
      new UpgradeContinuously(Protoss.ObserverSpeed)))
  
  class TemplarTech extends Parallel(
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives),
      Get(Protoss.Forge)),
    new If(
      new UnitsAtMost(0, Protoss.Observatory),
      new BuildCannonsAtNatural(2)))
  
  class Upgrades extends Parallel(
    new Build(Get(Protoss.Forge)),
    new If(
      new UnitsAtMost(0, Protoss.TemplarArchives, complete = true),
      new If(
        new UpgradeComplete(Protoss.GroundDamage, 1),
        new Build(Get(Protoss.GroundArmor)),
        new Build(Get(Protoss.GroundDamage))),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new If(
          new Or(
            new UnitsAtLeast(2, Protoss.Forge, complete = true),
            new UpgradeComplete(Protoss.GroundDamage, 3)),
          new UpgradeContinuously(Protoss.GroundArmor)))),
    new TemplarTech,
    new IfOnMiningBases(3, new Build(Get(2, Protoss.Forge))))

  class BuildTech extends Parallel(
    new Build(Get(1, Protoss.Gateway)),
    new Build(
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)),

    new Build(Get(3, Protoss.Gateway)),
    new BuildGasPumps,
    new FlipIf(
      new Latch(
        new Or(
          new UnitsAtLeast(1, Protoss.TemplarArchives),
          new And(
            new UnitsAtLeast(1, Protoss.PhotonCannon),
            new UnitsAtLeast(8, Protoss.Zealot)))),
      new RoboTech,
      new TemplarTech),

    new Build(Get(5, Protoss.Gateway)),

    new If(
      new Or(
        new GasPumpsAtLeast(3),
        new And(
          new GasPumpsAtLeast(2),
          new UnitsAtLeast(15, UnitMatchWarriors))),
        new OnGasPumps(3, new Upgrades)),

    new OnGasPumps(4, new Build(Get(Protoss.HighTemplarEnergy))))

  class ArbiterTransition extends Build(
    Get(Protoss.Stargate),
    Get(Protoss.ArbiterTribunal),
    Get(2, Protoss.Stargate),
    Get(Protoss.Stasis))

  class CarrierTransition extends Parallel(
    new Build(Get(Protoss.Stargate)),
    new UpgradeContinuously(Protoss.AirDamage),
    new Build(
      Get(Protoss.FleetBeacon),
      Get(2, Protoss.Stargate),
      Get(Protoss.CarrierCapacity),
      Get(3, Protoss.Stargate)))

  override val buildPlans = Vector(

    new If(
      new EnemyDarkTemplarLikely,
      new BuildCannonsAtBases(1)),

    new If(
      new Not(new EnemyCarriers),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new If(
      new Or(
        new UnitsAtLeast(3, Protoss.Dragoon),
        new And(
          new UnitsAtMost(0, Protoss.CitadelOfAdun, complete = true),
          new Not(new UpgradeComplete(Protoss.ZealotSpeed)))),
        new UpgradeContinuously(Protoss.DragoonRange)),

    new PvPIdeas.TakeBase2,

    new If(
      new Check(() => With.blackboard.keepingHighTemplar.get),
      new Build(Get(Protoss.PsionicStorm))),

    new PvPIdeas.TakeBase3,

    new If(
      new UnitsAtLeast(3,  Protoss.Reaver),
      new Build(Get(Protoss.ScarabDamage))),

    new If(
      new EnemiesAtLeast(1, Protoss.DarkTemplar),
      new UpgradeContinuously(Protoss.ObserverSpeed)),

    new FlipIf(
      new Or(
        new UnitsAtLeast(40, UnitMatchWarriors),
        new And(
          new UnitsAtLeast(25, UnitMatchWarriors),
          new SafeAtHome)),
      new PvPIdeas.TrainArmy,
      new BuildTech),

    new FlipIf(
      new SafeToMoveOut,
      new If(
        new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
        new Build(Get(6, Protoss.Gateway)),
        new Build(Get(8, Protoss.Gateway))),
      new RequireBases(3)),

    new Build(Get(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(Get(11, Protoss.Gateway)),
    new BuildCannonsAtExpansions(1),

    new If(
      new EnemiesAtLeast(3, Protoss.Shuttle),
      new Build(Get(Protoss.Stargate), Get(Protoss.Corsair))),

    new If(new SafeToMoveOut, new RequireMiningBases(4)),

    new If(
      new And(
        new SafeAtHome,
        new Or(
          new Employing(PvPLateGameCarrier),
          new UnitsAtLeast(8, Protoss.Arbiter)),
        new GasPumpsAtLeast(3)),
      new CarrierTransition,
      new Build(Get(12, Protoss.Gateway))),

    new If(
      new And(
        new Or(
          new Employing(PvPLateGameArbiter),
          new UnitsAtLeast(8, Protoss.Carrier)),
        new GasPumpsAtLeast(3)),
      new ArbiterTransition),
  
    new FlipIf(
      new SafeToMoveOut,
      new Build(Get(12, Protoss.Gateway)),
      new RequireMiningBases(4)),
  
    new FlipIf(
      new SafeToMoveOut,
      new Build(Get(20, Protoss.Gateway)),
      new RequireMiningBases(5)),
  
    new Build(Get(20, Protoss.Gateway)),
    new RequireMiningBases(6),
    new UpgradeContinuously(Protoss.Shields)
  )
}
