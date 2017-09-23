package Planning.Plans.Macro.Milestones

import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class EnemyUnitsNone(matcher: UnitMatcher = UnitMatchAnything) extends EnemyUnitsAtMost(0, matcher) {
  
  description.set("Enemy has no " + matcher)
}
