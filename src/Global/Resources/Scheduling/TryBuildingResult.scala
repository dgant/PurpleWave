package Global.Resources.Scheduling

import Types.Buildable.Buildable

class TryBuildingResult(
  val buildEvent:Option[SimulationEvent],
  val unmetPrerequisites:Iterable[Buildable] = List.empty,
  val exceededSearchDepth:Boolean = false)