package Global.Combat.Commands

import Types.Intents.Intention

trait Command {
  def execute(intent:Intention)
}
