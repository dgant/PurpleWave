package Global.Combat.Commands

import Types.Intents.Intention

object Avoid extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val enemyProximity = intent.battle.get.enemy.units.map(enemy => enemy.distance(unit) - enemy.range).min
    val doRetreat = enemyProximity < 32 * 4
    
    if (doRetreat) {
      Dodge.execute(intent)
    } else {
      Reenforce.execute(intent)
    }
  }
}
