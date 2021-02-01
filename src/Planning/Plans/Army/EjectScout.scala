package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEjectScout
import Planning.Plans.Scouting.{ScoutCleared, ScoutTracking}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers._
import Utilities.{ByOption, Minutes}

class EjectScout extends Squadify[GoalEjectScout] {

  override val goal: GoalEjectScout = new GoalEjectScout
  private val scoutCleared = new ScoutCleared

  override def update() {
    if (With.frame > Minutes(8)()) return
    if (scoutCleared.isComplete) return

    val scouts = ScoutTracking.enemyScouts.toSeq
    val scout = ByOption.minBy(scouts)(_.id)

    if (scouts.isEmpty) return

    squad.enemies = ScoutTracking.enemyScouts.toSeq
    goal.unitMatcher = UnitMatchAnd(UnitMatchCanCatchScouts, (unit) => unit.base.exists(b => b.isOurMain || b.isNaturalOf.exists(_.isOurMain)) || unit.pixelsToGetInRange(scout.get) < 32)
    goal.unitCounter = UnitCountOne
    super.update()
  }
}
