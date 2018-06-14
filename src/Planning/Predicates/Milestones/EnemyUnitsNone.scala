package Planning.Predicates.Milestones

import Planning.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class EnemyUnitsNone(matcher: UnitMatcher = UnitMatchAnything) extends EnemyUnitsAtMost(0, matcher)