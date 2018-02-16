package Planning.Plans.Predicates

import Lifecycle.With
import Planning.Plans.Compound.Check
import Planning.Yolo

class SafeToAttack extends Check(() => Yolo.active || With.battles.global.globalSafeToAttack)