package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.Buildable

class SimulationEvent(
  val buildable:Buildable,
  val frameStart:Int,
  val frameEnd:Int,
  val isImplicit:Boolean = false) {
  
  override def toString: String = buildable.toString + (if (isImplicit) "*" else "")
  
  def formatTime(time:Int):String = {
    val relativeTime = time - With.game.getFrameCount
    if (relativeTime < 0) "Now" else relativeTime.toString
  }
}