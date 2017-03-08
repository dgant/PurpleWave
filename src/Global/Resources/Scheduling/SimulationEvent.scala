package Global.Resources.Scheduling

import Types.Buildable.Buildable

class SimulationEvent(
  val frameEnd:Int,
  val buildable:Buildable)
    extends Ordered[SimulationEvent] {
  
  override def compare(that: SimulationEvent): Int = frameEnd.compare(that.frameEnd)
}