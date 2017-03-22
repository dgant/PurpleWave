package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  val ranged = new TargetingProfile(
    preferInRange     = 1,
    preferValue       = 0.5,
    preferDps         = 0.5,
    avoidHealth       = 2,
    avoidDistance     = 2,
    avoidDistraction  = 0.25)
  
  val melee = new TargetingProfile(
    preferInRange     = 1,
    preferValue       = 0.25,
    preferDps         = 0.25,
    avoidHealth       = 2,
    avoidDistance     = 3,
    avoidDistraction  = 0.75)
}
