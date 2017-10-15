package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Plans.Compound.And
import Planning.Plans.Information.Matchup.EnemyRaceKnown

class GameplanModeTemplateVsRandom extends GameplanModeTemplate {
  
  def completionCriteriaAdditional = new Plan
  
  final override val completionCriteria = new And(
    new EnemyRaceKnown,
    completionCriteriaAdditional)
}
