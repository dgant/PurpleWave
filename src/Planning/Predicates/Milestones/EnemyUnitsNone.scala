package Planning.Predicates.Milestones

import Planning.UnitMatchers.{MatchAnything, Matcher}

class EnemyUnitsNone(matcher: Matcher = MatchAnything) extends EnemiesAtMost(0, matcher)