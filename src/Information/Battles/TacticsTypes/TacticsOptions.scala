package Information.Battles.TacticsTypes

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
  
  override def toString: String = {
    val names = Map(
      Tactics.Movement.None     -> "Movement: None",
      Tactics.Movement.Charge   -> "Movement: Charge",
      Tactics.Movement.Kite     -> "Movement: Kite",
      Tactics.Movement.Flee     -> "Movement: Flee",
      Tactics.Focus.None        -> "Focus: None",
      Tactics.Focus.Air         -> "Focus: Air",
      Tactics.Focus.Ground      -> "Focus: Ground",
      Tactics.Wounded.Fight     -> "Wounded: Fight",
      Tactics.Wounded.Flee      -> "Wounded: Flee",
      Tactics.Workers.Ignore    -> "Workers: Ignore",
      Tactics.Workers.Flee      -> "Workers: Flee",
      Tactics.Workers.FightAll  -> "Workers: Fight (All)",
      Tactics.Workers.FightHalf -> "Workers: Fight (Half)"
    )
    names.filter(namePair => has(namePair._1)).mkString(", ")
  }
  
  override def hashCode(): Int = options
  override def equals(obj: scala.Any): Boolean = hashCode == obj.hashCode && obj.isInstanceOf[TacticsOptions]
}
