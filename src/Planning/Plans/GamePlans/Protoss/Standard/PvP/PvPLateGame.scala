package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.ReactToDarkTemplarEmergencies
import Planning.Plans.Macro.Automatic.TrainWorkersContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.GasAtMost
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyCarriers
import Planning.Plans.Predicates.{Employing, SafeAtHome, SafeToAttack}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPLateGameCarrier, PvPLateGameGateway}

class PvPLateGame extends GameplanModeTemplate {
  
  override val scoutExpansionsAt = 90
  override val emergencyPlans: Vector[Plan] = Vector(new ReactToDarkTemplarEmergencies)
  
  override def aggression: Double = 0.85
  
  override def defaultWorkerPlan: Plan = new If(
    new SafeAtHome,
    new TrainWorkersContinuously(true),
    new TrainWorkersContinuously(false))
  
  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val defaultAttackPlan = new PvPIdeas.AttackSafely
  
  override def defaultArchonPlan: Plan = new PvPIdeas.MeldArchonsPvP
  
  class RoboTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)),
    new If(
      new UnitsAtMost(0, Protoss.TemplarArchives),
      new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
    new If(
      new EnemyHasShownCloakedThreat,
      new UpgradeContinuously(Protoss.ObserverSpeed)))
  
  class TemplarTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives)),
    new If(
      new UnitsAtMost(0, Protoss.Observatory),
      new BuildCannonsAtNatural(2)))
  
  class Upgrades extends Parallel(
    new Build(RequestAtLeast(1, Protoss.Forge)),
    new If(
      new UpgradeComplete(Protoss.GroundDamage, 3),
      new UpgradeContinuously(Protoss.GroundArmor),
      new UpgradeContinuously(Protoss.GroundDamage)))
  
  class BuildTech extends Parallel(
    new Build(RequestAtLeast(1, Protoss.Gateway)),
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.DragoonRange)),
    new If(
      new GasAtMost(300),
      new BuildGasPumps),
    
    new FlipIf(
      new Latch(new UnitsAtLeast(1, Protoss.TemplarArchives)),
      
      // Robo first (default)
      new Parallel(
        new RoboTech,
        new Build(RequestAtLeast(2, Protoss.Gateway)),
        new BuildGasPumps,
        new Build(RequestAtLeast(5, Protoss.Gateway)),
        new Upgrades),
      
      // Citadel first (ie. DT follow-up)
      new Parallel(
        new Build(RequestAtLeast(3, Protoss.Gateway)),
        new TemplarTech,
        new BuildGasPumps,
        new Build(RequestAtLeast(5, Protoss.Gateway)))),
    
    new If(
      new Not(new EnemyCarriers),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new OnGasPumps(3, new Build(RequestUpgrade(Protoss.HighTemplarEnergy))))
  
  class ArbiterTransition extends Build(
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(1, Protoss.ArbiterTribunal),
    RequestTech(Protoss.Stasis))
  
  class CarrierTransition extends Parallel(
    new Build(RequestAtLeast(1, Protoss.Stargate)),
    new UpgradeContinuously(Protoss.AirDamage),
    new Build(
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(2, Protoss.Stargate),
      RequestUpgrade(Protoss.CarrierCapacity),
      RequestAtLeast(3, Protoss.Stargate)))
  
  override val buildPlans = Vector(
    new If(new UnitsAtLeast(1,  Protoss.Dragoon),         new Build(RequestUpgrade(Protoss.DragoonRange))),
    new PvPIdeas.TakeBase2,
    new If(new UnitsAtLeast(1,  Protoss.HighTemplar),     new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2,  Protoss.Reaver),          new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new PvPIdeas.TakeBase3,
    new If(new EnemyUnitsAtLeast(1, Protoss.DarkTemplar), new UpgradeContinuously(Protoss.ObserverSpeed)),
  
    new BuildCannonsAtExpansions(3),
  
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
      new SafeToAttack,
      new If(
        new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
        new Build(RequestAtLeast(6, Protoss.Gateway)),
        new Build(RequestAtLeast(8, Protoss.Gateway))),
      new RequireMiningBases(3)),
  
    new FlipIf(
      new SafeToAttack,
      new If(
        new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
        new Build(RequestAtLeast(9, Protoss.Gateway)),
        new Build(RequestAtLeast(11, Protoss.Gateway))),
      new RequireMiningBases(4)),
      
    new If(
      new And(
        new Or(
          new Employing(PvPLateGameCarrier),
          new UnitsAtLeast(8, Protoss.Arbiter)),
        new HaveGasPumps(3)),
      new CarrierTransition,
      new Build(RequestAtLeast(12, Protoss.Gateway))),
  
    new If(
      new And(
        new Or(
          new Employing(PvPLateGameGateway),
          new UnitsAtLeast(8, Protoss.Carrier)),
        new HaveGasPumps(3)),
      new ArbiterTransition),
  
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
  
    new FlipIf(
      new SafeToAttack,
      new If(
        new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
        new Build(RequestAtLeast(12, Protoss.Gateway)),
        new Build(RequestAtLeast(14, Protoss.Gateway))),
      new RequireMiningBases(5)),
      
    new RequireMiningBases(6),
    new UpgradeContinuously(Protoss.Shields)
  )
}
