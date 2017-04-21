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
    avoidTraffic      = 0.25)
  
  def kite =  new MovementProfile(
    preferDestination = 0.50,
    preferOrigin      = 0.50,
    preferSitAtRange  = 1.25,
    preferMobility    = 1.25,
    avoidDamage       = 1.75,
    avoidTraffic      = 1.50)
  
  def flee =  new MovementProfile(
    preferOrigin      = 1.00,
    preferMobility    = 1.25,
    avoidDamage       = 1.75,
    avoidTraffic      = 1.25)
}
