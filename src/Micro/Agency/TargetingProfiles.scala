package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    = 1.0,
    preferVpfOurs     = 1.0,
    preferDetectors   = 4.0,
    preferFocusFire   = 1.0,
    avoidPain         = 2.0,
    avoidDelay        = 0.02,
    avoidInterceptors = 2.0)
}
