package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.UnitMatchers.UnitMatcher
import Utilities.ByOption

class GoalHunt(val enemyMatcher: UnitMatcher) extends SquadGoalBasic {

  override def inherentValue: Double = GoalValue.harass
  
  override def toString: String = f"Hunt ${enemyMatcher.toString.replaceAll("Match", "")} with ${unitMatcher.toString.replaceAll("UnitMatch", "")} in ${target.zone}"

  var target: Pixel = With.scouting.mostBaselikeEnemyTile.pixelCenter
  
  override def run() {
    target = chooseTarget()
    squad.units.foreach(attacker => {
      attacker.agent.intend(this, new Intention {
        toTravel = Some(target)
      })
    })
  }
  
  protected def chooseTarget(): Pixel = {
    val targets = With.units.enemy.filter(enemyMatcher).filter(_.likelyStillThere)
    val centroid = PurpleMath.centroid(targets.map(_.pixel))
    val flying = squad.units.forall(_.flying)
    ByOption
      .minBy(targets.map(_.pixel))(_.pixelDistance(centroid))
      .getOrElse(With.scouting.baseIntrigue.maxBy(_._2)._1.heart.pixelCenter)
  }
}
