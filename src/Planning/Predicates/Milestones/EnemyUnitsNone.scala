package Planning.Predicates.Milestones

import Planning.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class EnemyUnitsNone(matcher: UnitMatcher = UnitMatchAnything) extends EnemiesAtMost(0, matcher)