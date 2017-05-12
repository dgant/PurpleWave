package Information.Battles.TacticsTypes

object Tactics {
  
  /*
  The obvious question is, why is this not just an enumeration?
  
  That's because Tactics are (well, were) used extensively in battle simulation, which is very performance sensitive,
  and Scala enum comparisons are a little slower than necessary.
  */
  
  type Tactic = Int
  
  private var nextV = 1
  private def v:Tactic = {
    nextV *= 2
    nextV / 2
  }
  
  val NoTactic = 0
  
  object Movement {
    val Advance = v
    val Retreat = v
    
    val values = Vector(Advance, Retreat)
  }
}
