package Micro.Squads.Goals

import Micro.Squads.Squad

trait SquadGoal {
  
  def update(squad: Squad)
  
  override lazy val toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}
