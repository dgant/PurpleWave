package Micro.Coordination.Pushing

import Debugging.Visualizations.Colors
import bwapi.Color

object PushPriority {
  private var nextValue: Int = 0
  private def next: Int = {
    nextValue += 1
    nextValue - 1
  }

  val None = next // No push
  val Pardon = next // Trying to walk here
  val Nudge = next  // Trying to get into the fight
  val Bump = next  // Trying to retreat
  val Shove = next  // Trying to do something mission-critical (like construct a building)
  val Dodge = next  // Trying to dodge explosion
  val Dive = next   // Trying to dodge NUCLEAR EXPLOSION

  def color(value: Int): Color = {
    value match {
      case Pardon => Colors.DarkBlue
      case Nudge  => Colors.DarkGreen
      case Bump   => Colors.BrightGreen
      case Shove  => Colors.BrightYellow
      case Dodge  => Colors.BrightOrange
      case Dive   => Colors.BrightRed
      case _ => Colors.NeonViolet
    }
  }
}
