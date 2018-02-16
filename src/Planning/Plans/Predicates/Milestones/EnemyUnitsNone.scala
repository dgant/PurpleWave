package Planning.Plans.Predicates.Milestones

import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class EnemyUnitsNone(matcher: UnitMatcher = UnitMatchAnything) extends EnemyUnitsAtMost(0, matcher) {
  
  description.set("Enemy has no " + matcher)
}
