package Global.Resources.Scheduling

import Types.Buildable.Buildable

class ScheduleSimulationBuildResult(
  val buildEvent:Option[SimulationEvent],
  val unmetPrerequisites:Iterable[Buildable] = List.empty)