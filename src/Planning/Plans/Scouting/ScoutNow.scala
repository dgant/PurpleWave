package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Plans.Basic.Write

class ScoutNow(maxScouts: Int = 1) extends Write(With.blackboard.maximumScouts, maxScouts)