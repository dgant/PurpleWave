package Global.Resources.Scheduling

import Types.Buildable.Buildable

class ScheduleSimulationBuildResult(
  val buildable:Option[SimulationEvent],
  val unmetPrerequisites:Iterable[Buildable] = List.empty)