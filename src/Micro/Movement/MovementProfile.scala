package Micro.Movement

import Startup.With
import Micro.Intentions.Intention
import bwapi.TilePosition

class MovementProfile(
  var preferTravel      : Double = 0,
  var preferSpot        : Double = 0,
  var preferSitAtRange  : Double = 0,
  var preferMobility    : Double = 0,
  var preferHighGround  : Double = 0,
  var preferGrouping    : Double = 0,
  var preferRandom      : Double = 0,
  var avoidDamage       : Double = 0,
  var avoidTraffic      : Double = 0,
  var avoidVision       : Double = 0,
  var avoidDetection    : Double = 0)
    extends EvaluatePosition {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double =
    List(
      weigh(  travel      (intent, candidate)       , preferTravel),
      weigh(  spot        (intent, candidate)       , preferSpot),
      weigh(  sitAtRange  (intent, candidate) / 10  , preferSitAtRange),
      weigh(  mobility    (intent, candidate) / 10  , preferMobility),
      weigh(  highGround  (intent, candidate)       , preferHighGround),
      weigh(  grouping    (intent, candidate) / 100 , preferGrouping),
      weigh(  random      (intent, candidate)       , preferRandom),
      weigh(  enemyDamage (intent, candidate) / 100 , -avoidDamage),
      weigh(  traffic     (intent, candidate)       , -avoidTraffic),
      weigh(  visibility  (intent, candidate)       , -avoidVision),
      weigh(  detection   (intent, candidate)       , -avoidDetection)
    )
    .product
  
  //TODO: Could performance optimize by not actually evaluating if weight is 0
  //TODO: Consolidate with TargetProfile
  def weigh(value:Double, weight:Double):Double = Math.pow(normalize(value), weight)
  def unboolify(value:Boolean)                  = if (value) 2 else 1
  def normalize(value:Double)                   = Math.min(Math.max(0.01, value), 1000000)
  
  def travel(intent: Intention, candidate: TilePosition):Double = distance(intent, candidate, 32 * 8)
  
  def spot(intent: Intention, candidate: TilePosition):Double = distance(intent, candidate, 32 * 1)
  
  def sitAtRange(intent: Intention, candidate: TilePosition): Double = {
    if (intent.targets.isEmpty) return 1
    
    val minDistance = intent.targets.map(_.tileCenter.getDistance(candidate)).min
    Math.abs(minDistance - intent.unit.unitClass.maxAirGroundRange)
  }
  
  def distance(intent: Intention, candidate: TilePosition, margin: Int): Double = {
    if (intent.destination.isEmpty) return unboolify(false)
    
    val before = Math.max(0, With.paths.groundDistance(intent.unit.tileCenter, intent.destination.get) - margin)
    val after  = Math.max(0, With.paths.groundDistance(candidate,              intent.destination.get) - margin)
    unboolify(after < before)
  }
  
  def traffic(intent: Intention, candidate: TilePosition): Double = {
    With.grids.units.get(candidate)
      .filter(_ == intent.unit)
      .filter(_.flying)
      .map(_.unitClass.width).sum
  }
  
  def mobility(intent: Intention, candidate: TilePosition): Double = {
    With.grids.mobility.get(candidate)
  }
  
  def enemyDamage(intent: Intention, candidate: TilePosition): Double = {
    With.grids.enemyGroundStrength.get(candidate)
  }
  
  def grouping(intent: Intention, candidate: TilePosition): Double = {
    With.grids.friendlyGroundStrength.get(candidate)
  }
  
  def random(intent:Intention, candidate: TilePosition): Double = {
    val randomVariation = 1.0
    (1 - randomVariation) + randomVariation * MovementRandom.random.nextDouble
  }
  
  def highGround(intent: Intention, candidate: TilePosition): Double = {
    With.grids.altitudeBonus.get(candidate)
  }
  
  def visibility(intent: Intention, candidate: TilePosition): Double = {
    unboolify(With.grids.enemyVision.get(candidate))
  }
  
  def detection(intent: Intention, candidate: TilePosition): Double = {
    unboolify(With.grids.enemyDetection.get(candidate))
  }
}
