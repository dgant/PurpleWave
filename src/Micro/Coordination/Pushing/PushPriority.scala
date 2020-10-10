package Micro.Coordination.Pushing

object PushPriority {
  private var nextValue: Int = 0
  private def next: Int = {
    nextValue += 1
    nextValue - 1
  }

  val Pardon = next // Trying to walk here
  val Nudge = next  // Trying to get into the fight
  val Bump = next  // Trying to retreat
  val Shove = next  // Trying to do something mission-critical (like construct a building)
  val Dodge = next  // Trying to dodge explosion
  val Dive = next   // Trying to dodge NUCLEAR EXPLOSION
}
