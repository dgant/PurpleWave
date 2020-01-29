package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicates.Compound.{Check, Latch}

class EnemyWalledIn extends Latch(new Check(() =>
  With.geography.zones.exists(z => z.walledIn && ! z.owner.isUs)
))