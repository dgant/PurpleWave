package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.5,
    preferVpfOurs     =  0.5,
    preferDetectors   =  8.0,
    avoidDelay        =  1.0,
    avoidPain         =  1.0)
}
