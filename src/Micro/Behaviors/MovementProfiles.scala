package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val default = new MovementProfile (
    preferTravel      = 1.00,
    preferSitAtRange  = 1.30,
    preferTarget      = 0.80,
    preferMobility    = 0.80,
    preferRandom      = 0.03,
    preferMoving      = 0.03,
    avoidDamage       = 1.00,
    avoidTraffic      = 0.70)
  
}
