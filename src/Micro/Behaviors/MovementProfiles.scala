package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val default = new MovementProfile (
    preferTravel      = 1.00,
    preferSitAtRange  = 1.00,
    preferTarget      = 1.50,
    preferMobility    = 1.00,
    preferRandom      = 0.05,
    preferMoving      = 0.05,
    avoidDamage       = 2.00,
    avoidTraffic      = 1.00)
}
