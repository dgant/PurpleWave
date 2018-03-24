package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.5,
    preferVpfOurs     =  1.0,
    preferDetectors   =  12.0,
    avoidPain         =  0.0,
    avoidDelay        =  0.3)
}
