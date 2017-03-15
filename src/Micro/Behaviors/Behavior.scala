package Micro.Behaviors

import Micro.Intentions.Intention

trait Behavior {
  def execute(intent:Intention)
}
