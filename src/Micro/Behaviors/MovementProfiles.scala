package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  val defaultCombat = new MovementProfile (
    preferTravel      = 0.5,
    preferSpot        = 0.5,
    preferSitAtRange  = 2,
    preferMobility    = 0.5,
    preferHighGround  = 0.5,
    preferGrouping    = 0.25,
    preferRandom      = 0.05,
    avoidDamage       = 2,
    avoidTraffic      = 1.5)
  
  val defaultNormal = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 1,
    preferHighGround  = 0.25,
    preferRandom      = 0.25,
    preferMoving      = 0.1,
    avoidTraffic      = 2)
  
  val worker = new MovementProfile (
    preferSpot        = 3,
    preferRandom      = 0.1,
    avoidDamage       = 3)
  
  val darkTemplar = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 1,
    avoidDamage       = 1,
    avoidDetection    = 4)
  
  val carrier = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 1,
    preferSitAtRange  = 2,
    preferGrouping    = 0.5,
    preferMoving      = 1,
    avoidDamage       = 3)
  
  val corsair = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 1,
    preferGrouping    = 1,
    preferMoving      = 2,
    avoidDamage       = 3)
}
