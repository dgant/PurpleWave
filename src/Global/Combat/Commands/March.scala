package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object March extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    
    if ( ! unit.onCooldown) {
      intent.battle
        .map(_.enemy.units.minBy(_.distanceSquared(unit)))
        .filter(_.distance(unit) <= unit.range)
        .foreach(enemy => {
          With.commander.attack(this, unit, enemy)
          return
        })
    }
    if (unit.distance(intent.destination.get) < 32 * 12) {
      Fight.execute(intent)
    } else {
      With.commander.move(this, unit, intent.destination.get)
    }
  }
}
