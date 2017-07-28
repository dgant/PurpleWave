package Micro.Behaviors

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    = 1.0,
    preferVpfOurs     = 1.0,
    avoidPain         = 1.5,
    avoidDelay        = 1.5)
}
