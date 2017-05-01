package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination = 1.00,
    preferMobility    = 0.50,
    preferOrigin      = 0.25,
    avoidTraffic      = 0.50)
  
  def charge = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.25,
    preferThreatDistance  = 0.75,
    preferTarget          = 2.00,
    avoidTraffic          = 0.25)
  
  def kite =  new MovementProfile(
    preferDestination     = 0.25,
    preferOrigin          = 0.75,
    preferThreatDistance  = 1.25,
    preferTarget          = 1.25,
    preferMobility        = 1.25,
    avoidDamage           = 1.00,
    avoidTraffic          = 1.50)
  
  def flee =  new MovementProfile(
    preferOrigin          = 1.00,
    preferThreatDistance  = 1.50,
    preferMobility        = 1.25,
    avoidDamage           = 1.00,
    avoidTraffic          = 1.25)
}
