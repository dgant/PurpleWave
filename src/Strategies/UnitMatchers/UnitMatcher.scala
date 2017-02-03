package Strategies.UnitMatchers

trait UnitMatcher {
  def accept(unit:bwapi.Unit): Boolean
}
