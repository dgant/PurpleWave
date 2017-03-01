package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Pillage extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val destination = intent.destination.get
    val targets = With.units.enemy.filter(_.distance(destination) < 32 * 8)
    
    if (targets.nonEmpty) {
      intent.targetUnit = Some(targets.minBy(_.totalHealth))
    }
    
    Hunt.execute(intent)
  }
}
