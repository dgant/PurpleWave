package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound.{And, FlipIf, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseGateway

class PvT2BaseGateways extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvT2BaseGateway)
  override val completionCriteria = new MiningBasesAtLeast(3)
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
    
  override val buildPlans = Vector(
    new BuildCannonsAtExpansions(2),
    new FlipIf(
      new Or(
        new And(
          new UnitsAtLeast(6, UnitMatchWarriors),
          new UnitsAtLeast(1, Protoss.Observatory),
          new SafeAtHome),
        new UnitsAtLeast(20, UnitMatchWarriors)),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new Build(
          RequestAtLeast(2,   Protoss.Gateway),
          RequestAtLeast(1,   Protoss.RoboticsFacility),
          RequestAtLeast(3,   Protoss.Gateway),
          RequestAtLeast(1,   Protoss.Observatory),
          RequestAtLeast(11,  Protoss.Gateway)),
        new RequireMiningBases(3))))
}

