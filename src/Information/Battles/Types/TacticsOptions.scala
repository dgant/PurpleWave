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
  
  override def toString: String = {
    val names = Map(
      Tactics.MovementNone      -> "Movement: None",
      Tactics.MovementCharge    -> "Movement: Charge",
      Tactics.MovementKite      -> "Movement: Kite",
      Tactics.MovementFlee      -> "Movement: Flee",
      Tactics.FocusNone         -> "Focus: None",
      Tactics.FocusAir          -> "Focus: Air",
      Tactics.FocusGround       -> "Focus: Ground",
      Tactics.WoundedFight      -> "Wounded: Fight",
      Tactics.WoundedFlee       -> "Wounded: Flee",
      Tactics.WorkersIgnore     -> "Workers: Ignore",
      Tactics.WorkersFlee       -> "Workers: Flee",
      Tactics.WorkersFightAll   -> "Workers: Fight (All)",
      Tactics.WorkersFightHalf  -> "Workers: Fight (Half)"
    )
    names.get(options).getOrElse(options.toString)
  }
}
