package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.UnitMatchers.UnitMatcher
import Utilities.ByOption

class GoalHunt(val enemyMatcher: UnitMatcher) extends SquadGoalBasic {

  override def inherentValue: Double = GoalValue.harass
  
  override def toString: String = (
    "Hunt "
    + enemyMatcher.toString.replaceAll("UnitMatch", "")
    + " with "
    + unitMatcher.toString.replaceAll("UnitMatch", "")
    + " in "
    + target.zone.name)

  var target: Pixel = With.scouting.mostBaselikeEnemyTile.pixelCenter
  
  override def run() {
    squad.enemies = With.units.enemy.view.filter(_.is(enemyMatcher)).toSeq
    target = chooseTarget()
    squad.units.foreach(attacker => {
      attacker.agent.intend(squad.client, new Intention {
        toTravel = Some(target)
      })
    })
  }
  
  protected def chooseTarget(): Pixel = {
    val centroid = PurpleMath.centroid(squad.enemies.view.map(_.pixelCenter))
    val flying = squad.units.forall(_.flying)
    ByOption
      .minBy(squad.enemies.view.filter(_.likelyStillThere).map(_.pixelCenter))(_.pixelDistance(centroid))
      .getOrElse(With.scouting.baseIntrigue.maxBy(_._2)._1.heart.pixelCenter)
  }
}
