package Global.Resources.Scheduling

import Types.Buildable.Buildable

class ScheduleSimulationResult(
                                val suggestedEvents:Iterable[BuildEvent],
                                val simulatedEvents:Iterable[BuildEvent],
                                val unbuildable:Iterable[Buildable])