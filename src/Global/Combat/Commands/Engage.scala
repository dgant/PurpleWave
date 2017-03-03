package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Engage extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    intent.targetUnit = intent.battle.map(_.enemy.units.minBy(_.distanceSquared(unit)))
    val threat = With.grids.enemyGroundStrength.get(unit.position) / With.grids.friendlyGroundStrength.get(unit.position)
    
    if (unit.onCooldown && intent.targetUnit.exists(_.range < unit.range) && threat > 0) {
      Dodge.execute(intent)
    } else {
      Hunt.execute(intent)
    }
  }
}
