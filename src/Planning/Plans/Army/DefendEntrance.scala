package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatcher}
import Utilities.ByOption

class DefendEntrance(
  unitMatcher: UnitMatcher = UnitMatchWarriors,
  unitCounter: UnitCounter = UnitCountEverything)
  extends BasicSquad[GoalDefendZone] {
  
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
