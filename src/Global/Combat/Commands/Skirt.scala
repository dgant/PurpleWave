package Global.Combat.Commands

import Types.Intents.Intention

object Skirt extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val enemyProximity = intent.battle.get.enemy.units.map(enemy => enemy.distance(unit) + enemy.range).min
    val doRetreat = enemyProximity < 32 * 6
    
    if (doRetreat) {
      Dodge.execute(intent)
    } else {
      Engage.execute(intent)
    }
  }
}
