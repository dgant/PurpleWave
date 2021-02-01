package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers._
import Utilities.ByOption

class DefendEntrance extends Squadify[GoalDefendZone] {
  
  override val goal: GoalDefendZone = new GoalDefendZone
  goal.unitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers))
  goal.unitCounter = UnitCountEverything
  
  override def update() {
    // If we're defending a base already, let the defense squad recruit everyone for greater squad cohesion
    if (With.blackboard.defendingBase()) return

    goal.zone = ByOption
      .minBy(With.geography.ourBasesAndSettlements)(_.heart.groundPixels(With.scouting.threatOrigin))
      .map(_.zone)
      .getOrElse(With.geography.home.zone)
    super.update()
  }
}
