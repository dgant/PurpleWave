package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Engage extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    val attackableEnemies = intent.battle.map(battle => battle.enemy.units.filter(_.visible)).getOrElse(List.empty)
    if (attackableEnemies.nonEmpty) {
      intent.targetUnit = Some(attackableEnemies.minBy(enemy => enemy.distanceSquared(unit)))
    }
    
    val threat = With.grids.enemyGroundStrength.get(unit.position)
    
    if (unit.onCooldown && intent.targetUnit.exists(_.range < unit.range) && threat > 0) {
      Dodge.execute(intent)
    } else {
      Hunt.execute(intent)
    }
  }
}
