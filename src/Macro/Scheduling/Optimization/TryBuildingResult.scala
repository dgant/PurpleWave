package Macro.Scheduling.Optimization

import Macro.Buildables.Buildable
import Macro.Scheduling.BuildEvent

class TryBuildingResult(
  val buildEvent:Option[BuildEvent],
  val unmetPrerequisites:Iterable[Buildable] = Vector.empty,
  val exceededSearchDepth:Boolean = false)