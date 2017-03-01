package Global.Combat.Commands

import Types.Intents.Intention

object Engage extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    intent.targetUnit = intent.battle.map(_.enemy.units.minBy(_.distanceSquared(unit)))
    
    if (unit.onCooldown && intent.targetUnit.exists(_.range < unit.range)) {
      Dodge.execute(intent)
    } else {
      Hunt.execute(intent)
    }
  }
}
