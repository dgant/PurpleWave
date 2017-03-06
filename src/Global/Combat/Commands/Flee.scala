package Global.Combat.Commands
import Startup.With
import Types.Intents.Intention

object Flee extends Command {
  
  override def execute(intent: Intention) {
    intent.destination = With.geography.home
    Approach.execute(intent)
  }
}
