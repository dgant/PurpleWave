package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination = 1.00)
  
  def charge = new MovementProfile(
    preferSitAtRange  = 1.50,
    preferTarget      = 2.00)
  
  def kite =  new MovementProfile(
    preferDestination = -1.00,
    preferOrigin      = 0.50,
    preferSitAtRange  = 1.25,
    preferMobility    = 1.25,
    avoidDamage       = 1.50,
    avoidTraffic      = 1.00)
  
  def flee =  new MovementProfile(
    preferDestination = -1.00,
    preferOrigin      = 1.00,
    preferMobility    = 1.25,
    avoidDamage       = 1.50)
}
