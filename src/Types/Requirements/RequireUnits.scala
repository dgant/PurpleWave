package Types.Requirements

import UnitMatching.Matcher.UnitMatch
  
class RequireUnits (
  val unitMatcher:UnitMatch,
  val quantity:Integer)
    extends Requirement {
}
