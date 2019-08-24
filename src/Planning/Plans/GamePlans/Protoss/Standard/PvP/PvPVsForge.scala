package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Planning.Predicate
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones.{BasesAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Protoss

class PvPVsForge extends PvPRobo {

  // Locutus has two very silly openers that are very confusing to a bot
  // * Forge fast expansion
  // * Forge FAKE fast expansion into 3 proxy gates
  //
  // Reacting correctly to this is tricky, and I haven't figured out a good general-purpose way of doing so.
  // * If you leave your base, you die to proxy gate backstabs
  // * If you turtle on one base with nothing to show for it, you die to real FFE
  //
  // Proposed answer: One base speedlots?!?

  override val activationCriteria: Predicate = new And(new EnemyStrategy(With.fingerprints.earlyForge), new UnitsAtMost(0, Protoss.CitadelOfAdun))
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))
}
