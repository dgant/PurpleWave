package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.{PvT3BaseCarrier, PvT3BaseCorsair}

class PvT3BaseCarriers extends TemplateMode {
  
  override val activationCriteria = new Or(new Employing(PvT3BaseCarrier), new Employing(PvT3BaseCorsair))
  override val scoutExpansionsAt  = 60
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
  
  override val buildPlans = Vector(
    new BuildCannonsAtExpansions(3),
    new BuildCannonsAtNatural(1),
    new BuildGasPumps,
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new FlipIf(
      new UnitsAtLeast(30, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new PvTIdeas.TrainObservers,
        new If(new UnitsAtLeast(2, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
        new Build(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(5, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(6, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Forge),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestTech(Protoss.PsionicStorm),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(2, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.FleetBeacon)),
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new Build(RequestAtLeast(3, Protoss.Stargate)))),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(5),
    new UpgradeContinuously(Protoss.GroundArmor))
}

