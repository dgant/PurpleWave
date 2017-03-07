package Global.Combat.Commands

import Types.Intents.Intention

object Skirt extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val nearestEnemy = intent.battle.map(_.enemy.units.minBy(unit.distance))
    val nearestEnemyOutranged = nearestEnemy.exists(_.range < unit.range)
    
    if (nearestEnemyOutranged && ! unit.onCooldown) {
      Engage.execute(intent)
    }
    else if (unit.isMelee && nearestEnemy.exists(_.edgeDistance(unit) < unit.range - unit.utype.width) && ! unit.onCooldown) {
      intent.targetUnit = nearestEnemy
      Hunt.execute(intent)
    }
    else {
      Dodge.execute(intent)
    }
  }
}
