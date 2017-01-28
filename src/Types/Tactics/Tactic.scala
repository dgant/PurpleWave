package Types.Tactics

abstract class Tactic(var unit:bwapi.Unit) {
  def execute();
}
