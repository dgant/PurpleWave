package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicates.Compound.Check

class FoundEnemyBase extends Check(() => With.geography.enemyBases.nonEmpty)
