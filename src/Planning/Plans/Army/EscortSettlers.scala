package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Micro.Squads.Goals.GoalPush
import Micro.Squads.Squad
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Utilities.ByOption

class EscortSettlers extends Plan {
  
  val squad: Squad = new Squad(this)
  
  override def onUpdate() {
    
    val settler = ByOption.minBy(With.units.ours.filter(builder =>
      builder.agent.toBuildTile.exists(tile =>
        tile.zone.bases.exists(base =>
          base.townHall.forall( ! _.complete)))))(_.matchups.threats.exists(_.is(UnitMatchWarriors)))
    
    if (settler.isEmpty) return
    
    val destination   = settler.get.agent.toBuildTile.get
    val zone          = destination.zone
    val enemies       = settler.get.matchups.threats.toSet ++ zone.units.filter(u => u.isEnemy && u.likelyStillAlive && u.canAttack(settler.get))
    val enemyClosest  = ByOption.minBy(enemies)(_.pixelDistanceEdge(settler.get))
    val target        = destination.pixelCenter.project(enemyClosest.map(_.pixelCenter).getOrElse(SpecificPoints.middle), 32.0 * 8.0)
    
    squad.setGoal(new GoalPush(target))
    squad.enemies = enemies.toSeq
    squad.commission()
  }
}
