package Information.Battles.Types

object Tactics {
  
  type Tactic = Int
  
  private var nextV = 1
  private def v:Tactic = {
    nextV *= 2
    nextV / 2
  }
  
  val MovementNone      = v
  val MovementCharge    = v
  val MovementKite      = v
  val MovementFlee      = v
  
  val WoundedFight      = v
  val WoundedFlee       = v
  
  val WorkersIgnore     = v
  val WorkersFightAll   = v
  val WorkersFightHalf  = v
  val WorkersFlee       = v
  
  val FocusNone         = v
  val FocusGround       = v
  val FocusAir          = v
}
