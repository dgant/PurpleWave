package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Flee extends Command {
  
  def execute(intent:Intention) {
    intent.destination = Some(With.geography.home)
    March.execute(intent)
  }
  
}
