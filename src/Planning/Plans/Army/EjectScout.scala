package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEjectScout
import Planning.Plans.Scouting.{ScoutCleared, ScoutTracking}
import Planning.UnitCounters.{UnitCountOne, UnitCounter}
import Planning.UnitMatchers._
import Utilities.{ByOption, Minutes}

class EjectScout(
  matcher: UnitMatcher = UnitMatchCanCatchScouts,
  counter: UnitCounter = UnitCountOne)
  extends SquadPlan[GoalEjectScout] {

  override val goal: GoalEjectScout = new GoalEjectScout

  private val scoutCleared = new ScoutCleared
  override def onUpdate() {
    if (With.frame > Minutes(8)()) return
    if (scoutCleared.isComplete) return

    val scouts = ScoutTracking.enemyScouts.toSeq
    val scout = ByOption.minBy(scouts)(_.id)

    if (scouts.isEmpty) return

    squad.enemies = ScoutTracking.enemyScouts.toSeq
    goal.unitMatcher = matcher
    goal.unitCounter = counter
    super.onUpdate()
  }
}
