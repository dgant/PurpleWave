package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Approach extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val threat = With.grids.enemyGroundStrength.get(unit.position)
    
    if (threat == 0) {
      With.commander.move(this, unit, intent.destination.get)
    } else {
      Flee.execute(intent)
    }
  }
}
