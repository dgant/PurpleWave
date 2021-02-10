package Planning.Predicates.Milestones

import Planning.UnitMatchers.{MatchAnything, UnitMatcher}

class EnemyUnitsNone(matcher: UnitMatcher = MatchAnything) extends EnemiesAtMost(0, matcher)