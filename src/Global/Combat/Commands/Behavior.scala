package Global.Combat.Commands

import Types.Intents.Intention

trait Behavior {
  def execute(intent:Intention)
}
