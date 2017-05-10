package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.30,
    preferMobility        = 0.45,
    preferThreatDistance  = 0.35,
    preferTarget          = 2.75,
    avoidTraffic          = 0.20)
  
  def charge = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.25,
    preferMobility        = 0.50,
    preferThreatDistance  = 0.50,
    preferTarget          = 4.00,
    avoidTraffic          = 0.25)
}
