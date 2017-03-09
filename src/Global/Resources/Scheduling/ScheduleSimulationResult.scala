package Global.Resources.Scheduling

import Types.Buildable.Buildable

class ScheduleSimulationResult(
  val events:Iterable[SimulationEvent],
  val unbuildable:Iterable[Buildable])