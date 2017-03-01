package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object March extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    
    With.commander.move(this, unit, intent.destination.get)
  }
}
