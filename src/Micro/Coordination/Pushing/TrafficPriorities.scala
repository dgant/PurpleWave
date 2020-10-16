package Micro.Coordination.Pushing

import Debugging.Visualizations.Colors
import bwapi.Color

object TrafficPriorities {
  private var nextValue: Int = 0
  private def next: Int = {
    nextValue += 1
    nextValue - 1
  }

  val None    = TrafficPriority(next, Color.Black,          "None")   // No push
  val Pardon  = TrafficPriority(next, Colors.DarkBlue,      "Pardon") // Trying to walk here
  val Nudge   = TrafficPriority(next, Colors.DarkGreen,     "Nudge")  // Trying to get into the fight
  val Bump    = TrafficPriority(next, Colors.BrightGreen,   "Bump")   // Trying to retreat
  val Shove   = TrafficPriority(next, Colors.BrightYellow,  "Shove")  // Trying to do something mission-critical (like construct a building)
  val Dodge   = TrafficPriority(next, Colors.BrightOrange,  "Dodge")  // Trying to dodge explosion
  val Dive    = TrafficPriority(next, Colors.BrightRed,     "Dive")   // Trying to dodge NUCLEAR EXPLOSION
}
