package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  val default = new TargetingProfile(
    preferInRange     = 2,
    preferValue       = 0.25,
    preferDps         = 0.75,
    avoidHealth       = 0.75,
    avoidDistance     = 1,
    avoidDistraction  = 0)
}
