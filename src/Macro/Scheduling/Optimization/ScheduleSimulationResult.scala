package Macro.Scheduling.Optimization

import Macro.Buildables.Buildable
import Macro.Scheduling.BuildEvent

class ScheduleSimulationResult(
                                val suggestedEvents:Iterable[BuildEvent],
                                val simulatedEvents:Iterable[BuildEvent],
                                val unbuildable:Iterable[Buildable])