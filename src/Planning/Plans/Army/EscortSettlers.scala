package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Micro.Squads.Goals.SquadPush
import Micro.Squads.Squad
import Planning.Plan
import Utilities.ByOption

class EscortSettlers extends Plan {
  
  val squad: Squad = new Squad(this)
  val conscript: Conscript = new Conscript(squad)
  
  override def getChildren: Iterable[Plan] = Array(conscript)
  
  override def onUpdate() {
    
    val settler = ByOption.minBy(With.units.ours.filter(u => u.agent.toBuild.exists(_.isTownHall)))(_.id)
    
    if (settler.isEmpty) return
    
    val destination   = settler.get.agent.toBuildTile.get
    val zone          = destination.zone
    val enemies       = settler.get.matchups.threats.toSet ++ zone.units.filter(u => u.isEnemy && u.canAttack(settler.get))
    val enemyClosest  = ByOption.minBy(enemies)(_.pixelDistanceFast(settler.get))
    val target        = destination.pixelCenter.project(enemyClosest.map(_.pixelCenter).getOrElse(SpecificPoints.middle), 32.0 * 8.0)
    
    conscript.squad.goal    = new SquadPush(target)
    conscript.mustFight     = zone.bases.exists(_.owner.isUs)
    conscript.overkill      = if (conscript.mustFight) 1.5 else 2.0
    conscript.enemies       = enemies.toSeq
    conscript.update()
  }
}
