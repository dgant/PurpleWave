package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEjectScout
import Planning.UnitCounters.{UnitCountOne, UnitCounter}
import Planning.UnitMatchers._
import ProxyBwapi.Races.Zerg
import Utilities.ByOption

class EjectScout(
  attackerMatcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  attackerCounter: UnitCounter = UnitCountOne)
  extends SquadPlan[GoalEjectScout] {
  
  override val goal: GoalEjectScout = new GoalEjectScout
  
  override def onUpdate() {
    val scouts = With.geography.ourZones.flatMap(_.units.filter(u => u.possiblyStillThere && u.isEnemy && u.isAny(UnitMatchWorkers, Zerg.Overlord)))
    val scout = ByOption.minBy(scouts)(_.id)
    
    if (scouts.isEmpty) return
  
    squad.enemies = scout
    goal.scout = scout
    goal.unitMatcher = attackerMatcher
    goal.unitCounter = attackerCounter
    super.onUpdate()
  }
}
