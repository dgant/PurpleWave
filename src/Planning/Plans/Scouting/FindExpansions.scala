package Planning.Plans.Scouting

import Micro.Squads.Goals.GoalFindExpansions
import Planning.Plans.Army.SquadPlan
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran}

class FindExpansions(matcher: UnitMatcher = UnitMatchAnd(
  UnitMatchRecruitableForCombat,
  UnitMatchNot(UnitMatchWorkers),
  UnitMatchNot(Terran.Medic),
  UnitMatchNot(Protoss.Shuttle))) extends SquadPlan[GoalFindExpansions] {

  override val goal: GoalFindExpansions = new GoalFindExpansions

  override def onUpdate() {
    goal.unitMatcher = matcher
    super.onUpdate()
  }
}