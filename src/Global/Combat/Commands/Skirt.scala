package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Skirt extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val nearestEnemy = intent.battle.map(_.enemy.units.minBy(unit.distance))
    val nearestEnemyOutranged = nearestEnemy.exists(_.range < unit.range)
    val threat = With.grids.enemyGroundStrength.get(unit.tileCenter) / With.grids.friendlyGroundStrength.get(unit.tileCenter).toDouble
    
    if (threat > 1) {
      Dodge.execute(intent)
    }
    else if (unit.onCooldown && nearestEnemyOutranged) {
      Dodge.execute(intent)
    }
    else {
      Engage.execute(intent)
    }
  }
}
