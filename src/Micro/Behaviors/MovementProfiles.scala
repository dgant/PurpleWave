package Micro.Behaviors

import Micro.Movement.MovementProfile

object MovementProfiles {
  val defaultCombat = new MovementProfile (
    preferTravel      = 0.2,
    preferSpot        = 0,
    preferSitAtRange  = 1.5,
    preferMobility    = 2,
    preferHighGround  = 0.5,
    preferGrouping    = 0,
    preferRandom      = 0.1,
    avoidDamage       = 3,
    avoidTraffic      = 0.75,
    avoidVision       = 0,
    avoidDetection    = 0)
  
  val defaultNormal = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 0.5,
    preferMobility    = 0.25,
    preferHighGround  = 0.25,
    preferGrouping    = 0.1,
    preferRandom      = 0.2,
    avoidDamage       = 0,
    avoidTraffic      = 2,
    avoidVision       = 0,
    avoidDetection    = 0)
  
  val darkTemplar = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 0,
    preferSitAtRange  = 2,
    preferMobility    = 0,
    preferHighGround  = 0,
    preferGrouping    = 0,
    preferRandom      = 0,
    avoidDamage       = 1,
    avoidTraffic      = 0,
    avoidVision       = 0,
    avoidDetection    = 4)
  
  val carrier = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 0,
    preferSitAtRange  = 0,
    preferMobility    = 0,
    preferHighGround  = 0.5,
    preferGrouping    = 0.5,
    preferRandom      = 0,
    avoidDamage       = 3,
    avoidTraffic      = 0,
    avoidVision       = 0,
    avoidDetection    = 0)
}
