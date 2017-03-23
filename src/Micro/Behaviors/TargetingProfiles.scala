package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  val default = new TargetingProfile(
    preferInRange     = 1,
    preferValue       = 0.5,
    preferDps         = 0.5,
    avoidHealth       = 2,
    avoidDistance     = 2,
    avoidDistraction  = 0.25)
}
