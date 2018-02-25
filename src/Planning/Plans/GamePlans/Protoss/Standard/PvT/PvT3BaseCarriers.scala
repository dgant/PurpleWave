package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.Aggression
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.{Employ, Employing}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.{PvT3BaseCarrier, PvT3BaseCorsair, PvT3BaseReaverCarrier}

class PvT3BaseCarriers extends GameplanModeTemplate {
  
  override val activationCriteria     = new Or(new Employing(PvT3BaseCarrier), new Employing(PvT3BaseReaverCarrier), new Employing(PvT3BaseCorsair))
  override val scoutExpansionsAt      = 60
  override val emergencyPlans         = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan      = new PvTIdeas.AttackRespectingMines
  override def defaultAggressionPlan  = new If(
    new And(
      new UnitsAtLeast(1, Protoss.FleetBeacon),
      new UnitsAtMost(3, Protoss.Carrier)),
    new Aggression(0.7),
    new Aggression(0.85))
  
  override val buildPlans = Vector(
    new BuildCannonsAtExpansions(3),
    new BuildCannonsAtNatural(1),
    new PvTIdeas.Require2BaseTech,
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new FlipIf(
      new UnitsAtLeast(30, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new Build(
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility)),
        new Employ(
          PvT3BaseReaverCarrier,
          new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
        new Build(
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Forge),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.FleetBeacon)),
        new UpgradeContinuously(Protoss.AirDamage),
        new Build(
          RequestAtLeast(3, Protoss.Stargate),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(6, Protoss.Gateway),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestTech(Protoss.PsionicStorm)),
        new UpgradeContinuously(Protoss.AirArmor))),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(5),
    new UpgradeContinuously(Protoss.GroundArmor))
}

