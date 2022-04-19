package Tactic.Tactics

import Macro.Allocation.Prioritized

trait Tactic extends Prioritized {
  def launch(): Unit
}
