package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  val default = new TargetingProfile(
    preferInRange     = 2.0,
    preferValue       = 0.50,
    preferCombat      = 3.0,
    preferDps         = 0.50,
    avoidHealth       = 0.50,
    avoidDistance     = 1.25,
    avoidDistraction  = 0)
}
