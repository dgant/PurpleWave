package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferInRange     = 2.5,
    preferValue       = 0.5,
    preferCombat      = 1.0,
    preferDps         = 1.5,
    preferDamageType  = 1.0,
    avoidPain         = 1.0,
    avoidHealth       = 1.0,
    avoidDistance     = 1.0,
    avoidDistraction  = 1.0)
}
