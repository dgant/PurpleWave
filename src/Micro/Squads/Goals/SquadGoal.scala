package Micro.Squads.Goals

import Debugging.ToString
import Micro.Squads.Squad

trait SquadGoal extends SquadRecruiter{
  def run()
  var squad: Squad = _

  override def toString: String = ToString(this).replaceAllLiterally("Goal", "")
}