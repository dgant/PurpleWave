package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination = 1.00,
    preferMobility    = 0.50,
    preferOrigin      = 0.25,
    avoidTraffic      = 0.50)
  
  def charge = new MovementProfile(
    preferDestination = 1.00,
    preferOrigin      = 0.25,
    preferSitAtRange  = 2.00,
    preferHighGround  = 0.50,
    preferAttackSpeed = 1.25,
    avoidTraffic      = 0.25)
  
  def kite =  new MovementProfile(
    preferDestination = 0.50,
    preferOrigin      = 0.50,
    preferSitAtRange  = 1.25,
    preferMobility    = 1.00,
    preferHighGround  = 0.25,
    preferAttackSpeed = 1.25,
    avoidDamage       = 1.50,
    avoidTraffic      = 1.00)
  
  def flee =  new MovementProfile(
    preferOrigin      = 1.00,
    preferMobility    = 1.00,
    preferHighGround  = 0.25,
    avoidDamage       = 2.50,
    avoidTraffic      = 1.00)
}
