package Micro.Heuristics.Movement

import Debugging.Visualization.Colors
import Micro.Heuristics.MovementHeuristics._

class MovementProfile(
  val preferTravel      : Double = 0,
  val preferSpot        : Double = 0,
  val preferSitAtRange  : Double = 0,
  val preferTarget      : Double = 0,
  val preferMobility    : Double = 0,
  val preferHighGround  : Double = 0,
  val preferMoving      : Double = 0,
  val preferRandom      : Double = 0,
  val avoidDamage       : Double = 0,
  val avoidTraffic      : Double = 0,
  val avoidVision       : Double = 0) {
  
  def weightedHeuristics: Iterable[MovementHeuristicWeight] =
    List(
      new MovementHeuristicWeight(MovementHeuristicDestinationApproximate,  preferTravel,       Colors.MediumGreen),
      new MovementHeuristicWeight(MovementHeuristicDestinationExact,        preferSpot,         Colors.NeonGreen),
      new MovementHeuristicWeight(MovementHeuristicEnemyAtMaxRange,         preferSitAtRange,   Colors.MediumRed),
      new MovementHeuristicWeight(MovementHeuristicInRangeOfTarget,         preferTarget,       Colors.BrightBlue),
      new MovementHeuristicWeight(MovementHeuristicMobility,                preferMobility,     Colors.MediumOrange),
      new MovementHeuristicWeight(MovementHeuristicHighGround,              preferHighGround,   Colors.DarkBlue),
      new MovementHeuristicWeight(MovementHeuristicKeepMoving,              preferMoving,       Colors.MediumBlue),
      new MovementHeuristicWeight(MovementHeuristicRandom,                  preferRandom,       Colors.DarkGray),
      new MovementHeuristicWeight(MovementHeuristicExposureToDamage,        -avoidDamage,       Colors.NeonRed),
      new MovementHeuristicWeight(MovementHeuristicTraffic,                 -avoidTraffic,      Colors.NeonYellow),
      new MovementHeuristicWeight(MovementHeuristicEnemyVision,             -avoidVision,       Colors.MediumGray)
    )
  
  def adjustBy(other:MovementProfile):MovementProfile = {
    new MovementProfile(
      preferTravel        = preferTravel            + other.preferTravel,
      preferSpot          = preferSpot              + other.preferSpot,
      preferSitAtRange    = preferSitAtRange        + other.preferSitAtRange,
      preferTarget        = preferTarget            + other.preferTarget,
      preferMobility      = preferMobility          + other.preferMobility,
      preferHighGround    = preferHighGround        + other.preferHighGround,
      preferMoving        = preferMoving            + other.preferMoving,
      preferRandom        = preferRandom            + other.preferRandom,
      avoidDamage         = avoidDamage             + other.avoidDamage,
      avoidTraffic        = avoidTraffic            + other.avoidTraffic,
      avoidVision         = avoidVision             + other.avoidVision
    )
  }
}
