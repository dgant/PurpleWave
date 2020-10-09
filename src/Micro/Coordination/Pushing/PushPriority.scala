package Micro.Coordination.Pushing

object PushPriority {
  private var nextValue: Integer = 0
  private def next: Int = {
    nextValue += 1
    nextValue - 1
  }

  val Pardon = next
  val Nudge = next
  val Push = next
  val Shove = next
  val Dodge = next
  val Dive = next
}
