package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.25,
    preferMobility        = 0.50,
    preferThreatDistance  = 0.50,
    preferTarget          = 2.00,
    avoidTraffic          = 0.25,
    avoidDamage           = 0.50)
  
  def charge = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.25,
    preferMobility        = 0.50,
    preferThreatDistance  = 0.50,
    preferTarget          = 2.00,
    avoidTraffic          = 0.25,
    avoidDamage           = 0.25)
  
  def flee = new MovementProfile(
    preferOrigin          = 1.00,
    preferThreatDistance  = 1.00,
    preferMobility        = 1.00,
    avoidTraffic          = 1.00,
    avoidDamage           = 1.00)
}
