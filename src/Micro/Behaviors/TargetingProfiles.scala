package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferInRange     = 2.0,
    preferValue       = 0.5,
    preferCombat      = 1.0,
    preferDps         = 2.0,
    preferDamageType  = 1.0,
    avoidPain         = 3.0,
    avoidHealth       = 1.0,
    avoidDistance     = 1.0,
    avoidDistraction  = 1.0)
}
