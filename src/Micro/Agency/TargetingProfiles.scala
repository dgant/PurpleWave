package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    = 2.0,
    preferVpfOurs     = 2.0,
    preferDetectors   = 4.0,
    avoidPain         = 2.0,
    avoidDelay        = 1.0)
}
