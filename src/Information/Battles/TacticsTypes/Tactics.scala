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
    val Charge    = v
    val Regroup   = v
    val Flee      = v
    
    val values = Vector(Charge, Regroup, Flee)
  }
  
  object Wounded {
    val Fight = v
    val Flee  = v
    
    val values = Vector(Fight, Flee)
  }
  
  object Workers {
    val Ignore    = v
    val FightAll  = v
    val FightHalf = v
    val Flee      = v
    
    val values = Vector(Ignore, FightAll, FightHalf)
  }
  
  object Focus {
    val Neither = v
    val Ground  = v
    val Air     = v
    
    val values = Vector(Neither, Ground, Air)
  }
}
