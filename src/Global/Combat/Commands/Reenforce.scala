package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention

object Reenforce extends Command {
  
  def execute(intent:Intention) {
    With.commander.attack(this, intent.unit, intent.battle.get.us.vanguard)
  }
  
}
