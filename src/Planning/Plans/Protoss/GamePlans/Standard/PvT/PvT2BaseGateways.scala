package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, OnMiningBases, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseGateway

class PvT2BaseGateways extends TemplateMode {
  
  override val activationCriteria = new Employing(PvT2BaseGateway)
  override val completionCriteria = new MiningBasesAtLeast(3)
  override val scoutExpansionsAt  = 60
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
  
  override val buildPlans = Vector(
    new OnMiningBases(3, new BuildCannonsAtNatural(1)),
    new BuildCannonsAtExpansions(2),
    new If(new UnitsAtLeast(24, UnitMatchWarriors), new RequireMiningBases(3)),
    new FlipIf(
      new UnitsAtLeast(15, UnitMatchWarriors),
      new Parallel(
        new PvTIdeas.TrainArmy,
        new Build(
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(5, Protoss.Gateway))),
      new PvTIdeas.Require3BaseTech),
    new Build(
      RequestTech(Protoss.PsionicStorm),
      RequestAtLeast(10, Protoss.Gateway)))
}

