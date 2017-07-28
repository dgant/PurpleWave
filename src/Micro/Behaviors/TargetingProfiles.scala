package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferInRange     = 1.0,
    preferValue       = 0.5,
    preferCombat      = 1.0,
    preferDpf         = 2.0,
    preferDamageAgainst  = 1.0,
    avoidPain         = 3.0,
    avoidHealth       = 1.5,
    avoidDistance     = 1.0,
    avoidDistraction  = 1.0)
}
