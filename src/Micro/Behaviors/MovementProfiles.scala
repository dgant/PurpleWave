package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.25,
    preferMobility        = 0.50,
    preferThreatDistance  = 0.50,
    preferTarget          = 3.00,
    avoidTraffic          = 0.25)
  
  def charge = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.25,
    preferMobility        = 0.50,
    preferThreatDistance  = 0.50,
    preferTarget          = 4.00,
    avoidTraffic          = 0.25)
  
  def flee = new MovementProfile(
    preferOrigin          = 1.00,
    preferThreatDistance  = 1.00,
    preferMobility        = 1.00,
    avoidTraffic          = 0.50)
}
