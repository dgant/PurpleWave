package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val defaultCombat = new MovementProfile (
    preferTravel      = 0.50,
    preferSpot        = 1.50,
    preferSitAtRange  = 0.70,
    preferTarget      = 0.50,
    preferMobility    = 0.70,
    preferRandom      = 0.05,
    preferMoving      = 0.05,
    avoidDamage       = 0.60,
    avoidTraffic      = 0.30)
  
  val defaultNormal = new MovementProfile (
    preferTravel      = 1.0,
    preferSpot        = 0.10,
    preferRandom      = 0.05,
    preferMoving      = 0.05,
    avoidTraffic      = 0.75)
  
  val worker = new MovementProfile (
    preferSpot        = 1.0,
    preferRandom      = 0.05,
    preferMoving      = 0.05,
    avoidDamage       = 1.0)
  
  val darkTemplar = new MovementProfile (
    preferTravel      = 1.0,
    preferSpot        = 1.0,
    avoidDamage       = 1.0,
    avoidDetection    = 3.0)
  
  val carrier = new MovementProfile (
    preferTravel      = 2.0,
    preferSitAtRange  = 2.0,
    avoidDamage       = 1.0)
  
  val corsair = new MovementProfile (
    preferTravel      = 2.0,
    preferTarget      = 1.0,
    preferRandom      = 0.2,
    avoidDamage       = 1.5)
}
