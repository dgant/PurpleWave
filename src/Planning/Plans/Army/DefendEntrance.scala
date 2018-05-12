package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Micro.Squads.Squad
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Utilities.ByOption

class DefendEntrance extends Plan {
  
  val squad = new Squad(this)
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWarriors)
  defenders.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    val zone = ByOption
      .minBy(With.geography.ourBasesAndSettlements)(_.heart.groundPixels(With.intelligence.mostBaselikeEnemyTile))
      .map(_.zone)
      .getOrElse(With.geography.home.zone)
    
    defenders.get.acquire(this)
    squad.conscript(defenders.get.units)
    squad.setGoal(new GoalDefendZone(zone))
  }
}
