package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Plans.Basic.Write

class ScoutWithWorkers(maxScouts: Int = 1) extends Write(With.blackboard.maximumScouts, maxScouts)