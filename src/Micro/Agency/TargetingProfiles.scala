package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfOurs     =  0.25,
    preferDetectors   =  8.0,
    avoidDelay        =  0.1,
    avoidPain         =  1.0)
}
