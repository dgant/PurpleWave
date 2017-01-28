package UnitMatching.Matcher

trait UnitMatch {
  def accept(unit:bwapi.Unit): Boolean
}
