package Tactics

import Planning.Prioritized

trait Tactic extends Prioritized {
  def launch(): Unit
}
