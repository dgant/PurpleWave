package Micro.Squads.Goals

import Debugging.ToString
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Recruitment.SquadRecruiter

trait SquadGoal extends SquadRecruiter {
  def run(): Unit
  def destination: Pixel = With.scouting.mostBaselikeEnemyTile.pixelCenter
  override def toString: String = ToString(this)
}