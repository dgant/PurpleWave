package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._
import Utilities.ByOption

class DefendEntrance(
  unitMatcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  unitCounter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalDefendZone] {
  
  override val goal: GoalDefendZone = new GoalDefendZone
  
  override def onUpdate() {
    goal.unitMatcher = unitMatcher
    goal.unitCounter = unitCounter
    goal.zone = ByOption
      .minBy(With.geography.ourBasesAndSettlements)(_.heart.groundPixels(With.intelligence.mostBaselikeEnemyTile))
      .map(_.zone)
      .getOrElse(With.geography.home.zone)
    super.onUpdate()
  }
}
