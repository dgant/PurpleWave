package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.DefendZones
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainWorkersContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, MatchMiningBases, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.GasAtMost
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyCarriers
import Planning.Plans.Predicates.{Employing, SafeAtHome, SafeToMoveOut}
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
    new TrainWorkersContinuously(true),
    new TrainWorkersContinuously(false))
  
  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val defaultAttackPlan = new PvPIdeas.AttackSafely
  
  override def priorityDefensePlan: Plan = new DefendZones { defenderMatcher.set(Protoss.Corsair) }
  
  override def defaultArchonPlan: Plan = new PvPIdeas.MeldArchonsPvP
  
  class RoboTech extends Parallel(
    new Build(
      GetAtLeast(1, Protoss.RoboticsFacility),
      GetAtLeast(1, Protoss.Observatory)),
    new If(
      new UnitsAtMost(0, Protoss.TemplarArchives),
      new Build(GetAtLeast(1, Protoss.RoboticsSupportBay))),
    new If(
      new EnemyHasShownCloakedThreat,
      new UpgradeContinuously(Protoss.ObserverSpeed)))
  
  class TemplarTech extends Parallel(
    new Build(
      GetAtLeast(1, Protoss.CitadelOfAdun),
      GetAtLeast(1, Protoss.TemplarArchives)),
    new If(
      new UnitsAtMost(0, Protoss.Observatory),
      new BuildCannonsAtNatural(2)))
  
  class Upgrades extends Parallel(
    new Build(GetAtLeast(1, Protoss.Forge)),
    new If(
      new Or(
        new UnitsAtLeast(2, Protoss.Forge, complete = true),
        new UpgradeComplete(Protoss.GroundDamage, 3),
        new And(
          new UpgradeComplete(Protoss.GroundDamage),
          new UnitsAtMost(0, Protoss.TemplarArchives))),
      new UpgradeContinuously(Protoss.GroundArmor)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(GetAtLeast(2, Protoss.Forge)))
  
  class BuildTech extends Parallel(
    new Build(GetAtLeast(1, Protoss.Gateway)),
    new Build(
      GetAtLeast(1, Protoss.Assimilator),
      GetAtLeast(1, Protoss.CyberneticsCore),
      GetUpgrade(Protoss.DragoonRange)),
    new If(
      new GasAtMost(300),
      new BuildGasPumps),
  
    new If(
      new IfOnMiningBases(3),
      new Upgrades),
    
    new FlipIf(
      new Latch(new UnitsAtLeast(1, Protoss.TemplarArchives)),
      
      // Robo first (default)
      new Parallel(
        new RoboTech,
        new Build(GetAtLeast(2, Protoss.Gateway)),
        new BuildGasPumps,
        new Build(GetAtLeast(5, Protoss.Gateway))),
      
      // Citadel first (ie. DT follow-up)
      new Parallel(
        new Build(GetAtLeast(3, Protoss.Gateway)),
        new TemplarTech,
        new BuildGasPumps,
        new Build(GetAtLeast(5, Protoss.Gateway)))),
    
    new If(
      new Not(new EnemyCarriers),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new OnGasPumps(3, new Build(GetUpgrade(Protoss.HighTemplarEnergy))))
  
  class ArbiterTransition extends Build(
    GetAtLeast(1, Protoss.Stargate),
    GetAtLeast(1, Protoss.ArbiterTribunal),
    GetTech(Protoss.Stasis))
  
  class CarrierTransition extends Parallel(
    new Build(GetAtLeast(1, Protoss.Stargate)),
    new UpgradeContinuously(Protoss.AirDamage),
    new Build(
      GetAtLeast(1, Protoss.FleetBeacon),
      GetAtLeast(2, Protoss.Stargate),
      GetUpgrade(Protoss.CarrierCapacity),
      GetAtLeast(3, Protoss.Stargate)))
  
  override val buildPlans = Vector(
    new If(new UnitsAtLeast(1,  Protoss.Dragoon),         new Build(GetUpgrade(Protoss.DragoonRange))),
    new PvPIdeas.TakeBase2,
    new If(new UnitsAtLeast(1,  Protoss.HighTemplar),     new Build(GetTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2,  Protoss.Reaver),          new Build(GetUpgrade(Protoss.ScarabDamage))),
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
        new Build(GetAtLeast(6, Protoss.Gateway)),
        new Build(GetAtLeast(8, Protoss.Gateway))),
      new RequireBases(3)),
  
    new BuildCannonsAtExpansions(3),
    
    new Build(GetAtLeast(11, Protoss.Gateway)),
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
        new HaveGasPumps(3)),
      new CarrierTransition,
      new Build(GetAtLeast(12, Protoss.Gateway))),
  
    new If(
      new And(
        new Or(
          new Employing(PvPLateGameArbiter),
          new UnitsAtLeast(8, Protoss.Carrier)),
        new HaveGasPumps(3)),
      new ArbiterTransition),
  
    new FlipIf(
      new SafeToMoveOut,
      new Build(GetAtLeast(12, Protoss.Gateway)),
      new RequireMiningBases(4)),
  
    new If(
      new EnemiesAtLeast(3, Protoss.Shuttle),
      new Build(
        GetAtLeast(1, Protoss.Stargate),
        GetAtLeast(1, Protoss.Corsair))),
  
    new FlipIf(
      new SafeToMoveOut,
      new Build(GetAtLeast(20, Protoss.Gateway)),
      new RequireMiningBases(5)),
  
    new Build(GetAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(6),
    new UpgradeContinuously(Protoss.Shields)
  )
}
