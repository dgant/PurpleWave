package Global.Combat.Commands

import Types.Intents.Intention

object Engage extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    val enemies = intent.battle.map(_.enemy.units).getOrElse(List.empty)
    val closestEnemy = enemies.headOption
    intent.targetUnit = closestEnemy
    
    if (unit.onCooldown && closestEnemy.exists(_.range < unit.range)) {
      Dodge.execute(intent)
    }
    else {
      Hunt.execute(intent)
    }
  }
}
