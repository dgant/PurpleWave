package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  1.0,
    preferVpfOurs     =  1.0,
    preferDetectors   =  8.0,
    avoidDelay        =  0.8,
    avoidPain         =  1.0)
}
