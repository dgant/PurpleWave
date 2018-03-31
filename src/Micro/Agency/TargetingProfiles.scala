package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.25,
    preferVpfOurs     =  1.0,
    preferDetectors   =  8.0,
    avoidPain         =  0.0,
    avoidDelay        =  0.7)
}
