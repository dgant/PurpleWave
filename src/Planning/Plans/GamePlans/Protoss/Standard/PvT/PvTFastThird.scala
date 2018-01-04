package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTFastThird

class PvTFastThird extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTFastThird)
  override val completionCriteria = new MiningBasesAtLeast(3)
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
  override val aggression         = 0.6
  
  override val buildPlans = Vector(
    new FlipIf(
      new UnitsAtLeast(7, Protoss.Dragoon),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new RequireMiningBases(3),
        new Build(
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(8, Protoss.Gateway))
      )))
}
