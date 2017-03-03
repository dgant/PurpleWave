package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Flee extends Command {
  
  def execute(intent:Intention) {
    
    val unit = intent.unit
    intent.destination = Some(With.geography.home)
    
    if (intent.unit.distance(intent.destination.get) < 32 * 10) {
      Engage.execute(intent)
    }
    else {
      Dodge.execute(intent)
    }
  }
}
