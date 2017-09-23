package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT3BaseArbiter

class PvT3BaseArbiters extends TemplateMode {
  
  override val activationCriteria = new Employing(PvT3BaseArbiter)
  override val scoutExpansionsAt  = 60
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.AttackWithDarkTemplar
  override val defaultAttackPlan  = new PvTIdeas.ContainSafely
  
  override val buildPlans = Vector(
    new BuildCannonsAtExpansions(2),
    new BuildCannonsAtNatural(1),
    new BuildGasPumps,
    new FlipIf(
      new UnitsAtLeast(18, UnitMatchWarriors),
      new Parallel(
        new PvTIdeas.TrainArmy,
        new Build(RequestAtLeast(12, Protoss.Gateway))),
      new Parallel(
        new PvTIdeas.TrainObservers,
        new If(new UnitsAtLeast(2, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
        new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(RequestTech(Protoss.Stasis))),
        new PvTIdeas.Require3BaseTech,
        new RequireMiningBases(3),
        new Build(
          RequestAtLeast(1, Protoss.Stargate),
          RequestUpgrade(Protoss.HighTemplarEnergy),
          RequestAtLeast(1, Protoss.ArbiterTribunal),
          RequestUpgrade(Protoss.ArbiterEnergy),
          RequestAtLeast(2, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor))),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(5))
}

