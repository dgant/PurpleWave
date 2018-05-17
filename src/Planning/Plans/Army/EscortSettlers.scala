package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.GoalEscort
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchRecruitableForCombat, UnitMatchWarriors, UnitMatcher}
import Utilities.ByOption

class EscortSettlers(
    attackerMatcher: UnitMatcher = UnitMatchRecruitableForCombat,
    attackerCounter: UnitCounter = UnitCountEverything)
  extends SquadPlan[GoalEscort] {
  
  override val goal: GoalEscort = new GoalEscort
  
  override def onUpdate() {
    
    val settler = ByOption.minBy(With.units.ours.filter(builder =>
      builder.agent.toBuildTile.exists(tile =>
        tile.zone.bases.exists(base =>
          base.townHall.forall( ! _.complete)))))(_.matchups.threats.exists(_.is(UnitMatchWarriors)))
    
    if (settler.isEmpty) return
    
    val destination   = settler.get.agent.toBuildTile.get
    val enemies       = With.paths.zonePathUnits(settler.get.zone, destination.zone).filter(u => u.isEnemy)
    goal.principal    = settler
    squad.enemies     = enemies
    super.onUpdate()
  }
}
