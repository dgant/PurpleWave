package Global.Combat.Commands
import Startup.With
import Types.Intents.Intention

object Hunt extends Command {
  
  override def execute(intent: Intention) {
    
    val unit = intent.unit
    
    if (intent.targetUnit.isEmpty) {
      With.commander.attack(this, intent.unit, intent.destination)
    }
    else if ( ! unit.onCooldown) {
      With.commander.attack(this, intent.unit, intent.targetUnit.get.position)
    }
  }
}
