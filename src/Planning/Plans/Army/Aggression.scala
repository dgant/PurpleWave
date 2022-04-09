package Planning.Plans.Army

import Lifecycle.With
import Planning.Plans.Basic.Write

class Aggression(aggressionRatio: Double) extends Write(With.blackboard.aggressionRatio, () => aggressionRatio)