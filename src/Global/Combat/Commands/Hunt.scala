package Global.Combat.Commands
import Startup.With
import Types.Intents.Intention

object Hunt extends Command {
  
  override def execute(intent: Intention) {
    
    val unit = intent.unit
    
    if (intent.targetUnit.isEmpty) {
      With.commander.attack(this, intent.unit, intent.destination.get)
    }
    else if (unit.onCooldown) {
      if (unit.isMelee) {
        With.commander.move(this, intent.unit, intent.targetUnit.get.position)
      }
    } else {
      With.commander.attack(this, intent.unit, intent.targetUnit.get.position)
    }
  }
}
