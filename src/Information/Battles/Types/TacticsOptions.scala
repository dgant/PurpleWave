package Information.Battles.Types

class TacticsOptions(private var options:Int = 0) {
  
  def add(tactic:Int) {
    options |= tactic
  }
  
  def has(tactic:Int):Boolean = {
    (options & tactic) > 0
  }
  
  def merge(other:TacticsOptions):TacticsOptions = {
    new TacticsOptions(options | other.options)
  }
}
