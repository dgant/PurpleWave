package Information.Battles.TacticsTypes

object Tactics {
  
  /*
  The obvious question is, why is this not just an enumeration?
  
  That's because Tactics are used extensively in battle simulation, which is very performance sensitive,
  and Scala enum comparisons are a little slower than necessary
  
   */
  
  type Tactic = Int
  
  private var nextV = 1
  private def v:Tactic = {
    nextV *= 2
    nextV / 2
  }
  
  object Movement {
    val None      = v
    val Charge    = v
    val Kite      = v
    val Flee      = v
    
    val values = Vector(None, Charge, Kite, Flee)
  }
  
  object Wounded {
    val Fight = v
    val Flee  = v
    
    val values = Vector(Fight, Flee)
  }
  
  object Workers {
    val Ignore     = v
    val FightAll   = v
    val FightHalf  = v
    val Flee       = v
    
    val values = Vector(Ignore, FightAll, FightHalf, Flee)
  }
  
  object Focus {
    val None    = v
    val Ground  = v
    val Air     = v
    
    val values = Vector(None, Ground, Air)
  }
}
