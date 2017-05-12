package Information.Battles.TacticsTypes

class TacticsOptions(private var options:Int = 0) {
  
  def add(tactic:Int) {
    options |= tactic
  }
  
  def has(tactic:Int):Boolean = {
    (options & tactic) > 0
  }
  
  override def toString: String = {
    val names = Vector(
      Tactics.Movement.Advance   -> "Movement: Charge",
      Tactics.Movement.Retreat     -> "Movement: Flee",
      Tactics.Movement.Regroup  -> "Movement: Regroup",
      Tactics.Focus.Air         -> "Focus: Air",
      Tactics.Focus.Ground      -> "Focus: Ground",
      Tactics.Wounded.Fight     -> "Wounded: Fight",
      Tactics.Wounded.Flee      -> "Wounded: Flee",
      Tactics.Workers.Flee      -> "Workers: Flee",
      Tactics.Workers.FightAll  -> "Workers: Fight (All)",
      Tactics.Workers.FightHalf -> "Workers: Fight (Half)"
    )
    names.filter(namePair => has(namePair._1)).map("[" + _._2 + "]").mkString(" ").replaceAllLiterally("[] ","").replaceAllLiterally("[]","")
  }
  
  override def hashCode(): Int = options
  override def equals(obj: scala.Any): Boolean = hashCode == obj.hashCode && obj.isInstanceOf[TacticsOptions]
}
