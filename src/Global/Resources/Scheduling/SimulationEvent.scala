package Global.Resources.Scheduling

import Types.Buildable.Buildable

class SimulationEvent(
  val buildable:Buildable,
  val frameStart:Int,
  val frameEnd:Int,
  val implicitEvent:Boolean)
    extends Ordered[SimulationEvent] {
  
  override def compare(that: SimulationEvent): Int = frameEnd.compare(that.frameEnd)
  
  override def toString: String = {
    buildable + ": " + frameStart + " - " + frameEnd
  }
}