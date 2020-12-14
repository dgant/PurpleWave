package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._

class DefendNatural(
  unitMatcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  unitCounter: UnitCounter = UnitCountEverything)

  extends SquadPlan[GoalDefendZone] {
  
  override val goal: GoalDefendZone = new GoalDefendZone
  
  override def onUpdate() {
    val bases = With.geography.ourBasesAndSettlements

    goal.unitMatcher = unitMatcher
    goal.unitCounter = unitCounter
    goal.zone = With.geography.ourNatural.zone
    super.onUpdate()
  }
}
