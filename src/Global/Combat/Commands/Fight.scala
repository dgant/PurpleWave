package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Fight extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    
    val closestEnemy = intent.battle.map(_.enemy.units.minBy(_.distanceSquared(unit)))
    if (unit.onCooldown) {
      if (closestEnemy.exists(unit.distance(_) > unit.range))
      With.commander.move(this, unit, closestEnemy.get.position)
    } else {
      closestEnemy.foreach(enemy => With.commander.attack(this, unit, enemy))
    }
  }
}
