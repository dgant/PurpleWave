package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferSame        = 2.0,
    preferInRange     = 5.0,
    preferCombat      = 1.0,
    preferDps         = 1.5,
    preferDamageType  = 1.0,
    avoidHealth       = 1.0,
    avoidDistance     = 1.0,
    avoidDistraction  = 1.0)
}
