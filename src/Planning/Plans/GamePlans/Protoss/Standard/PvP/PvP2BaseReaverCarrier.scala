package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, DefendZones}
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, MatchMiningBases, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.GasAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyCarriers, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPLateGameArbiter, PvPLateGameCarrier}

class PvP2BaseReaverCarrier extends GameplanModeTemplate {
  
  override val scoutExpansionsAt = 150
  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush
  )
  
  override def defaultWorkerPlan: Plan = new If(
    new SafeAtHome,
    new PumpWorkers(true),
    new PumpWorkers(false))

  override val defaultAttackPlan = new If(new UnitsAtLeast(24, Protoss.Interceptor), new Attack)
  
  class RoboTech extends Parallel(
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory)),
    new If(
      new UnitsAtMost(0, Protoss.TemplarArchives),
      new Build(Get(1, Protoss.RoboticsSupportBay))),
    new If(
      new EnemyHasShownCloakedThreat,
      new UpgradeContinuously(Protoss.ObserverSpeed)))
  
  class TemplarTech extends Parallel(
    new Build(
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives)),
    new If(
      new UnitsAtMost(0, Protoss.Observatory),
      new BuildCannonsAtNatural(2)))
  
  class Upgrades extends Parallel(
    new Build(Get(1, Protoss.Forge)),
    new If(
      new Or(
        new UnitsAtLeast(2, Protoss.Forge, complete = true),
        new UpgradeComplete(Protoss.GroundDamage, 3),
        new And(
          new UpgradeComplete(Protoss.GroundDamage),
          new UnitsAtMost(0, Protoss.TemplarArchives))),
      new UpgradeContinuously(Protoss.GroundArmor)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(Get(2, Protoss.Forge)))

  class BuildTech extends Parallel(
    new Build(Get(1, Protoss.Gateway)),
    new Build(
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)),
    new If(
      new GasAtMost(300),
      new BuildGasPumps),
  
    new If(
      new MiningBasesAtLeast(3),
      new Upgrades),
    
    new FlipIf(
      new Latch(new UnitsAtLeast(1, Protoss.TemplarArchives)),
      
      // Robo first (default)
      new Parallel(
        new RoboTech,
        new Build(Get(2, Protoss.Gateway)),
        new BuildGasPumps,
        new Build(Get(5, Protoss.Gateway))),
      
      // Citadel first (ie. DT follow-up)
      new Parallel(
        new Build(Get(3, Protoss.Gateway)),
        new TemplarTech,
        new BuildGasPumps,
        new Build(Get(5, Protoss.Gateway)))),
    
    new If(
      new Not(new EnemyCarriers),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new OnGasPumps(3, new Build(Get(Protoss.HighTemplarEnergy))))
  
  class ArbiterTransition extends Build(
    Get(1, Protoss.Stargate),
    Get(1, Protoss.ArbiterTribunal),
    Get(Protoss.Stasis))
  
  class CarrierTransition extends Parallel(
    new Build(Get(1, Protoss.Stargate)),
    new UpgradeContinuously(Protoss.AirDamage),
    new Build(
      Get(1, Protoss.FleetBeacon),
      Get(2, Protoss.Stargate),
      Get(Protoss.CarrierCapacity),
      Get(3, Protoss.Stargate)))
  
  override val buildPlans = Vector(
    new If(new UnitsAtLeast(1,  Protoss.Dragoon),         new Build(Get(Protoss.DragoonRange))),
    new PvPIdeas.TakeBase2,
    new If(new UnitsAtLeast(1,  Protoss.HighTemplar),     new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2,  Protoss.Reaver),          new Build(Get(Protoss.ScarabDamage))),
    new PvPIdeas.TakeBase3,
    new If(new EnemiesAtLeast(1, Protoss.DarkTemplar), new UpgradeContinuously(Protoss.ObserverSpeed)),
  
    new If(
      new SafeAtHome,
      new MatchMiningBases),
    
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
  
    new BuildCannonsAtExpansions(3),
    
    new Build(Get(11, Protoss.Gateway)),
    new RequireMiningBases(3),
  
    new If(
      new SafeToMoveOut,
      new RequireMiningBases(4)),
    
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
  
    new If(
      new EnemiesAtLeast(3, Protoss.Shuttle),
      new Build(
        Get(1, Protoss.Stargate),
        Get(1, Protoss.Corsair))),
  
    new FlipIf(
      new SafeToMoveOut,
      new Build(Get(20, Protoss.Gateway)),
      new RequireMiningBases(5)),
  
    new Build(Get(20, Protoss.Gateway)),
    new RequireMiningBases(6),
    new UpgradeContinuously(Protoss.Shields)
  )
}
