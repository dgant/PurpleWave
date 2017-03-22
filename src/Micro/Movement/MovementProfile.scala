package Micro.Movement

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

import Utilities.TypeEnrichment.EnrichPosition._

class MovementProfile(
  var preferTravel      : Double = 0,
  var preferSpot        : Double = 0,
  var preferSitAtRange  : Double = 0,
  var preferMobility    : Double = 0,
  var preferHighGround  : Double = 0,
  var preferGrouping    : Double = 0,
  var preferMoving      : Double = 0,
  var preferRandom      : Double = 0,
  var avoidDamage       : Double = 0,
  var avoidTraffic      : Double = 0,
  var avoidVision       : Double = 0,
  var avoidDetection    : Double = 0)
    extends EvaluatePosition {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double =
    List(
      weigh(intent, candidate,  travel,       preferTravel),
      weigh(intent, candidate,  spot,         preferSpot),
      weigh(intent, candidate,  sitAtRange,   preferSitAtRange),
      weigh(intent, candidate,  mobility,     preferMobility),
      weigh(intent, candidate,  highGround,   preferHighGround),
      weigh(intent, candidate,  grouping,     preferGrouping),
      weigh(intent, candidate,  moving,       preferMoving),
      weigh(intent, candidate,  random,       preferRandom),
      weigh(intent, candidate,  enemyDamage,  -avoidDamage),
      weigh(intent, candidate,  traffic,      -avoidTraffic),
      weigh(intent, candidate,  visibility,   -avoidVision),
      weigh(intent, candidate,  detection,    -avoidDetection)
    )
    .product
  
  //TODO: Consolidate with TargetProfile
  def weigh(
    intent:Intention,
    candidate:TilePosition,
    value:(Intention, TilePosition) => Double,
    weight:Double):Double
  = if (weight == 0) 1 else Math.pow(normalize(value(intent, candidate)), weight)
  
  def unboolify(value:Boolean) = if (value) 2 else 1
  def normalize(value:Double)  = Math.min(Math.max(0.01, value), 1000000)
  
  def travel(intent: Intention, candidate: TilePosition):Double = distance(intent, candidate, 32 * 8)
  def spot  (intent: Intention, candidate: TilePosition):Double = distance(intent, candidate, 32 * 1)
  
  def sitAtRange(intent: Intention, candidate: TilePosition): Double = {
    if (intent.targets.isEmpty) return 1
    
    val minDistance = intent.targets.map(_.tileCenter.getDistance(candidate)).min
    Math.abs(minDistance - intent.unit.unitClass.maxAirGroundRange)
  }
  
  def distance(intent: Intention, candidate: TilePosition, margin: Int): Double = {
    if (intent.destination.isEmpty) return unboolify(false)
    
    val before = Math.max(0, getDistance(intent, intent.unit.tileCenter,  intent.destination.get) - margin)
    val after  = Math.max(0, getDistance(intent, candidate,               intent.destination.get) - margin)
    unboolify(after < before)
  }
  
  def getDistance(intent: Intention, origin:TilePosition, destination:TilePosition):Double = {
    intent.unit.travelPixels(destination)
  }
  
  def traffic(intent: Intention, candidate: TilePosition): Double = {
    if (intent.unit.flying) 1 else
      With.grids.units.get(candidate)
        .filterNot(_ == intent.unit)
        .filterNot(_.flying)
        .map(_.unitClass.width)
        .sum
  }
  
  def mobility(intent: Intention, candidate: TilePosition): Double = {
    if (intent.unit.flying) 1 else
      With.grids.mobility.get(candidate) / 10.0
  }
  
  def enemyDamage(intent: Intention, candidate: TilePosition): Double = {
    With.grids.enemyStrength.get(candidate) / 100.0
  }
  
  def grouping(intent: Intention, candidate: TilePosition): Double = {
    With.grids.friendlyStrength.get(candidate)  / 100.0
  }
  
  def moving(intent:Intention, candidate:TilePosition):Double = {
    intent.unit.pixelDistance(candidate.pixelCenter)
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
