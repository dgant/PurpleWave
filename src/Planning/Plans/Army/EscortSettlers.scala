package Planning.Plans.Army

import Lifecycle.With
import Micro.Squads.Goals.{SquadChill, SquadGoal, SquadPush}
import Planning.Plan
import Utilities.ByOption

class EscortSettlers extends Plan {
  
  val recruit: Conscript = new Conscript
  var goal: SquadGoal = new SquadChill
  
  override def getChildren: Iterable[Plan] = Array(recruit)
  
  override def onUpdate() {
    
    val settler = ByOption.minBy(With.units.ours.filter(u => u.agent.toBuild.exists(_.isTownHall)))(_.id)
    
    if (settler.isEmpty) return
    
    val destination = settler.get.agent.toBuildTile.get
    val zone        = destination.zone
    val enemies     = settler.get.matchups.threats.toSet ++ zone.units.filter(u => u.isEnemy && u.canAttack(settler.get))
    
    goal = new SquadPush(destination.pixelCenter)
    
    recruit.squad.goal  = goal
    recruit.mustFight   = zone.bases.exists(_.owner.isUs)
    recruit.overkill    = if (recruit.mustFight) 1.5 else 2.0
    recruit.enemies     = enemies.toSeq
    recruit.update()
  }
}
