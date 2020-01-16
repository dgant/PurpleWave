package Micro.Squads.Goals

import Micro.Squads.Squad

trait SquadGoal {
  var squad: Squad = _
  def run()
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}
