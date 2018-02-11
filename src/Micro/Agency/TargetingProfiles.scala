package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.5,
    preferVpfOurs     =  1.0,
    preferDetectors   =  12.0,
    preferFocusFire   =  0.1,
    avoidPain         =  2.5,
    avoidDelay        =  0.175,
    avoidInterceptors =  2.0)
}
