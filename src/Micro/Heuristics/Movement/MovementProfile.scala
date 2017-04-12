package Micro.Heuristics.Movement

import Debugging.Visualizations.Colors
import Micro.Heuristics.MovementHeuristics._

class MovementProfile(
  var preferDestination : Double = 0,
  var preferOrigin      : Double = 0,
  var preferSitAtRange  : Double = 0,
  var preferTarget      : Double = 0,
  var preferMobility    : Double = 0,
  var preferHighGround  : Double = 0,
  var preferAttackSpeed : Double = 0,
  var preferMoving      : Double = 0,
  var preferRandom      : Double = 0,
  var avoidDamage       : Double = 0,
  var avoidTraffic      : Double = 0,
  var avoidVision       : Double = 0) {
  
  def weightedHeuristics: Iterable[MovementHeuristicWeight] =
    Vector(
      new MovementHeuristicWeight(MovementHeuristicDestination,             preferDestination,  Colors.MediumGreen),
      new MovementHeuristicWeight(MovementHeuristicOrigin,                  preferOrigin,       Colors.NeonGreen),
      new MovementHeuristicWeight(MovementHeuristicEnemyAtMaxRange,         preferSitAtRange,   Colors.MediumRed),
      new MovementHeuristicWeight(MovementHeuristicInRangeOfTarget,         preferTarget,       Colors.BrightBlue),
      new MovementHeuristicWeight(MovementHeuristicMobility,                preferMobility,     Colors.MediumOrange),
      new MovementHeuristicWeight(MovementHeuristicHighGround,              preferHighGround,   Colors.DarkBlue),
      new MovementHeuristicWeight(MovementHeuristicAttackSpeed,             preferAttackSpeed,  Colors.BrightViolet),
      new MovementHeuristicWeight(MovementHeuristicKeepMoving,              preferMoving,       Colors.MediumBlue),
      new MovementHeuristicWeight(MovementHeuristicRandom,                  preferRandom,       Colors.DarkGray),
      new MovementHeuristicWeight(MovementHeuristicExposureToDamage,        -avoidDamage,       Colors.NeonRed),
      new MovementHeuristicWeight(MovementHeuristicTraffic,                 -avoidTraffic,      Colors.NeonYellow),
      new MovementHeuristicWeight(MovementHeuristicEnemyVision,             -avoidVision,       Colors.MediumGray)
    )
  
  def combine(other:MovementProfile) {
    preferDestination   += other.preferDestination
    preferOrigin        += other.preferOrigin
    preferSitAtRange    += other.preferSitAtRange
    preferTarget        += other.preferTarget
    preferMobility      += other.preferMobility
    preferHighGround    += other.preferHighGround
    preferAttackSpeed   += other.preferAttackSpeed
    preferMoving        += other.preferMoving
    preferRandom        += other.preferRandom
    avoidDamage         += other.avoidDamage
    avoidTraffic        += other.avoidTraffic
    avoidVision         += other.avoidVision
  }
}
