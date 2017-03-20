package Micro.Behaviors

import Micro.Targeting.TargetingProfile

object TargetingProfiles {
  val default = new TargetingProfile(
    preferInRange     = .75,
    preferValue       = 0.2,
    preferDps         = 2,
    avoidHealth       = 2,
    avoidDistance     = 0.2)
}
