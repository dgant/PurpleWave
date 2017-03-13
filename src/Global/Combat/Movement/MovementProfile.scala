package Global.Combat.Movement

import Startup.With
import Types.Intents.Intention
import bwapi.TilePosition

class MovementProfile(
  var preferTravel      : Double = 0,
  var preferMobility    : Double = 0,
  var preferHighGround  : Double = 0,
  var preferGrouping    : Double = 0,
  var avoidDamage       : Double = 0,
  var avoidTraffic      : Double = 0,
  var avoidVision       : Double = 0,
  var avoidDetection    : Double = 0)
    extends EvaluatePosition {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double =
    List(
      weigh(  travel      (intent, candidate)       , preferTravel),
      weigh(  mobility    (intent, candidate) / 10  , preferMobility),
      weigh(  highGround  (intent, candidate)       , preferHighGround),
      weigh(  grouping    (intent, candidate) / 100 , preferGrouping),
      weigh(  enemyDamage (intent, candidate) / 100 , -avoidDamage),
      weigh(  traffic     (intent, candidate)       , -avoidTraffic),
      weigh(  visibility  (intent, candidate)       , -avoidVision),
      weigh(  detection   (intent, candidate)       , -avoidDetection)
    )
    .product
  
  //TODO: Consolidate with TargetProfile
  def weigh(value:Double, weight:Double):Double = Math.pow(normalize(value), weight)
  def unboolify(value:Boolean)                  = if (value) 2 else 1
  def normalize(value:Double)                   = Math.max(0, value)
  
  def travel(intent: Intention, candidate: TilePosition): Double = {
    val before = With.paths.groundDistance(intent.unit.tileCenter, intent.destination)
    val after = With.paths.groundDistance(candidate,               intent.destination)
    unboolify(after < before)
  }
  
  def traffic(intent: Intention, candidate: TilePosition): Double = {
    (With.grids.units.get(candidate) -- List(intent.unit)).map(_.utype.width).sum
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
