package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Pillage extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val destination = intent.destination
    val targets = With.units.enemy
      .filterNot(_.flying) //Hack -- zealots trying to pillage Overlords
      .filter(_.visible)
      .filter(_.distance(destination) < 32 * 8)
    
    if (targets.nonEmpty) {
      intent.targetUnit = Some(targets.minBy(_.totalHealth))
    }
    
    Hunt.execute(intent)
  }
}
