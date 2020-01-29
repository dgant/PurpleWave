package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEjectScout
import Planning.UnitCounters.{UnitCountOne, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.Races.Zerg
import Utilities.ByOption

class EjectScout(
  matcher: UnitMatcher = UnitMatchCanCatchScouts,
  counter: UnitCounter = UnitCountOne)
  extends SquadPlan[GoalEjectScout] {

  override val goal: GoalEjectScout = new GoalEjectScout

  override def onUpdate() {
    val eligibleZones = With.geography.ourZones.toSet ++ Seq(With.geography.ourNatural.zone) ++ With.geography.ourMain.zone.edges.flatMap(_.zones)
    val scouts = eligibleZones.flatMap(_.units.filter(u => u.possiblyStillThere && u.isEnemy && u.isAny(UnitMatchWorkers, Zerg.Overlord)))
    val scout = ByOption.minBy(scouts)(_.id)

    if (scouts.isEmpty) return

    squad.enemies = scout.toSeq
    goal.scout = scout
    goal.unitMatcher = matcher
    goal.unitCounter = counter
    super.onUpdate()
  }
}
