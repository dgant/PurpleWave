package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.ReactToDarkTemplarEmergencies
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.GasAtMost
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyCarriers
import Planning.Plans.Predicates.SafeAtHome
import ProxyBwapi.Races.Protoss

class PvPLateGame extends GameplanModeTemplate {
  
  override val scoutExpansionsAt = 90
  
  override val emergencyPlans: Vector[Plan] = Vector(new ReactToDarkTemplarEmergencies)
  
  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val defaultAttackPlan = new PvPIdeas.AttackSafely
  
  override def defaultArchonPlan: Plan = new PvPIdeas.MeldArchonsPvP
  
  class BuildTech extends Parallel(
    new Build(RequestAtLeast(1, Protoss.Gateway)),
    new If(
      new GasAtMost(300),
      new BuildGasPumps),
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(4, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(1, Protoss.TemplarArchives)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new OnGasPumps(3,
      new Build(
        RequestAtLeast(2, Protoss.Forge),
        RequestUpgrade(Protoss.HighTemplarEnergy))))
  
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
  
    new FlipIf(
      new Or(
        new UnitsAtLeast(40, UnitMatchWarriors),
        new And(
          new UnitsAtLeast(25, UnitMatchWarriors),
          new SafeAtHome)),
      new Parallel(
        new PvPIdeas.TrainArmy,
        new If(
          new And(
            new Not(new EnemyCarriers),
            new Not(new SafeAtHome)),
          new BuildCannonsAtNatural(4)),
      new BuildCannonsAtExpansions(2)),
      new BuildTech),
    new FlipIf(
      new SafeAtHome,
      new Build(RequestAtLeast(8, Protoss.Gateway)),
      new RequireMiningBases(3)),
    new OnGasPumps(3,
      new CarrierTransition,
      new Build(RequestAtLeast(12, Protoss.Gateway))),
    new RequireMiningBases(4),
    new FlipIf(
      new SafeAtHome,
      new Build(RequestAtLeast(12, Protoss.Gateway)),
      new RequireMiningBases(5))
  )
}
