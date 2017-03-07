package Global.Combat.Commands

import Types.Intents.Intention

object Skirt extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val nearestEnemy = intent.battle.map(_.enemy.units.minBy(unit.distance))
    val nearestEnemyOutranged = nearestEnemy.exists(_.range < unit.range)
    
    if (intent.motivation < 1) {
      Dodge.execute(intent)
    }
    else if (unit.onCooldown && nearestEnemyOutranged && intent.motivation < 5) {
      Dodge.execute(intent)
    }
    else {
      Engage.execute(intent)
    }
  }
}
