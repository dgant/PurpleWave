package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.5,
    preferVpfOurs     =  1.0,
    preferDetectors   =  10.0,
    preferFocusFire   =  0.5,
    avoidPain         =  2.0,
    avoidDelay        =  0.2,
    avoidInterceptors =  2.0)
}
