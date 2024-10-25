package Tactic.Squads

object AttackModes {
  trait AttackMode
  object YOLO         extends AttackMode { override val toString = "AllIn"    }
  object RazeBase     extends AttackMode { override val toString = "Raze"     }
  object RazeProxy    extends AttackMode { override val toString = "Deprox"   }
  object PushMain     extends AttackMode { override val toString = "PushMain" }
  object PushExpo     extends AttackMode { override val toString = "PushExpo" }
  object CrushArmy    extends AttackMode { override val toString = "Crush"    }
  object ContainArmy  extends AttackMode { override val toString = "Contain"  }
  object Backstab     extends AttackMode { override val toString = "Backstab" }
  object ClearMap     extends AttackMode { override val toString = "Clear"    }
}
