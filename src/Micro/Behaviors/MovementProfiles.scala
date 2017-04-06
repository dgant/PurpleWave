package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val defaultCombat = new MovementProfile (
    preferTravel      = 1.00,
    preferSitAtRange  = 1.30,
    preferTarget      = 0.80,
    preferMobility    = 0.80,
    preferRandom      = 0.03,
    preferMoving      = 0.03,
    avoidDamage       = 1.00,
    avoidTraffic      = 0.70)
  
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
