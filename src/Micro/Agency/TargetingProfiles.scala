package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  1.0,
    preferVpfOurs     =  1.0,
    preferDetectors   =  8.0,
    avoidDelay        =  1.0,
    avoidPain         =  1.0)
}
