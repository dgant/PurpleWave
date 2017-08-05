package Micro.Squads.Goals

import Micro.Squads.Squad

trait SquadGoal {
  
  def update(squad: Squad)
  
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}
