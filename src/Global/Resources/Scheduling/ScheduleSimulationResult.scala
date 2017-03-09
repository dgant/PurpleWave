package Global.Resources.Scheduling

import Types.Buildable.Buildable

class ScheduleSimulationResult(
  val suggestedEvents:Iterable[SimulationEvent],
  val simulatedEvents:Iterable[SimulationEvent],
  val unbuildable:Iterable[Buildable])