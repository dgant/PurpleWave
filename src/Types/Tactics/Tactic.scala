package Types.Tactics

abstract class Tactic(var unit:bwapi.Unit) {
  def isComplete():Boolean = false
  def execute();
}
