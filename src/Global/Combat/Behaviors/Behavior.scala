package Global.Combat.Behaviors

import Types.Intents.Intention

trait Behavior {
  def execute(intent:Intention)
}
