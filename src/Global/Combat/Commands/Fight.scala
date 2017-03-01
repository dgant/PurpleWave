package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Fight extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    val closestEnemy = intent.battle.map(_.enemy.units.minBy(_.distanceSquared(unit)))
    
    if (unit.onCooldown) {
      if (closestEnemy.isEmpty) {
        With.commander.attack(this, unit, intent.destination.get)
      }
      else {
        if (closestEnemy.get.range < unit.range) {
          Dodge.execute(intent)
        } else {
          With.commander.attack(this, unit, closestEnemy.get)
        }
      }
      
    } else {
      closestEnemy.foreach(enemy => With.commander.attack(this, unit, enemy))
    }
  }
}
