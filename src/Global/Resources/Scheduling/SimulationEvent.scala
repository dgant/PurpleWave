package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.Buildable

class SimulationEvent(
  val buildable:Buildable,
  val frameStart:Int,
  val frameEnd:Int,
  val isImplicit:Boolean = false)
    extends Ordered[SimulationEvent] {
  
  override def compare(that: SimulationEvent): Int = frameEnd.compare(that.frameEnd)
  
  override def toString: String = {
    buildable +
      ": " +
      formatTime(frameStart) +
      " to " +
      formatTime(frameEnd)
  }
  
  def formatTime(time:Int):String = {
    val relativeTime = time - With.game.getFrameCount
    if (relativeTime < 0) "Now" else relativeTime.toString
  }
}