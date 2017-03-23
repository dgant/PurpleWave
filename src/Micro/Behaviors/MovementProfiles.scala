package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val defaultCombat = new MovementProfile (
    preferTravel      = 2.0,
    preferSpot        = 0.5,
    preferSitAtRange  = 1.0,
    preferMobility    = 0.75,
    preferHighGround  = 0.25,
    preferGrouping    = 0.15,
    avoidDamage       = 1.0,
    avoidTraffic      = 0.5)
  
  val defaultNormal = new MovementProfile (
    preferTravel      = 2.5,
    preferSpot        = 1.0,
    preferHighGround  = 0.1,
    preferRandom      = 0.1,
    preferMoving      = 0.1,
    avoidTraffic      = 0.5)
  
  val worker = new MovementProfile (
    preferSpot        = 3.0,
    preferRandom      = 0.1,
    avoidDamage       = 3.0)
  
  val darkTemplar = new MovementProfile (
    preferTravel      = 1.0,
    preferSpot        = 1.0,
    avoidDamage       = 1.0,
    avoidDetection    = 3.0)
  
  val carrier = new MovementProfile (
    preferTravel      = 2.0,
    preferSpot        = 0.5,
    preferSitAtRange  = 2.0,
    preferGrouping    = 0.15,
    preferMoving      = 0.5,
    avoidDamage       = 1.0)
  
  val corsair = new MovementProfile (
    preferTravel      = 2.0,
    preferSpot        = 0.5,
    preferGrouping    = 0.15,
    preferMoving      = 0.5,
    avoidDamage       = 1.0)
}
