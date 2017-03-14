package Global.Resources.Scheduling

import Types.Buildable.Buildable

class TryBuildingResult(
                         val buildEvent:Option[BuildEvent],
                         val unmetPrerequisites:Iterable[Buildable] = List.empty,
                         val exceededSearchDepth:Boolean = false)