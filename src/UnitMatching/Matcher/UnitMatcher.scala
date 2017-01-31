package UnitMatching.Matcher

trait UnitMatcher {
  def accept(unit:bwapi.Unit): Boolean
}
