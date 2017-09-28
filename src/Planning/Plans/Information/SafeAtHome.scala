package Planning.Plans.Information

import Lifecycle.With
import Planning.Plans.Compound.Check
import Planning.Yolo

class SafeAtHome extends Check(() => Yolo.active || With.battles.global.globalSafeToDefend)