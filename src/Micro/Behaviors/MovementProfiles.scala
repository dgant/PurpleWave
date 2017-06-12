package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.30,
    preferMobility        = 0.45,
    preferThreatDistance  = 0.10,
    preferTargetValue     = 1.00,
    avoidTraffic          = 0.20,
    avoidDamage           = 0.25)
  
  def approach = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.50,
    avoidDamage           = 2.00
  )
  
  def flee = new MovementProfile(
    preferOrigin          = 1.00,
    preferThreatDistance  = 0.20,
    avoidDamage           = 1.00
  )
  
  def charge = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.30,
    preferMobility        = 0.45,
    preferThreatDistance  = 0.10,
    preferTargetValue     = 2.00,
    avoidTraffic          = 0.20,
    avoidDamage           = 0.25)
}
