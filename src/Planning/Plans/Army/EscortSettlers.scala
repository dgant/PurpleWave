package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEscort
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers._
import Utilities.ByOption

class EscortSettlers(
  matcher: UnitMatcher = UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers)),
  counter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalEscort] {
  
  override val goal: GoalEscort = new GoalEscort
  
  override def onUpdate() {
    
    val settler = ByOption.minBy(With.units.ours.filter(builder =>
      builder.is(UnitMatchWorkers)
      && builder.agent.toBuildTile.exists(
        _.base.exists(
          _.townHall.forall( ! _.complete)))))(_.matchups.threats.exists(_.is(UnitMatchWarriors)))
    
    if (settler.isEmpty) return
    
    val buildTile     = settler.get.agent.toBuildTile.get
    val enemies       = With.paths.zonePathUnits(settler.get.zone, buildTile.zone).filter(u => u.isEnemy)
    goal.principal    = settler
    squad.enemies     = (enemies ++ settler.get.matchups.threats).distinct
    super.onUpdate()
  }
}
