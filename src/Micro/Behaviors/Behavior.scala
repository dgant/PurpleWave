package Micro.Behaviors

import Micro.Intent.Intention

trait Behavior {
  def execute(intent:Intention)
}
