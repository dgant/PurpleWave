package Micro.Heuristics.Targeting
import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo

class TargetingProfile(
  var preferInRange     : Double = 0,
  var preferValue       : Double = 0,
  var preferDps         : Double = 0,
  var avoidHealth       : Double = 0,
  var avoidDistance     : Double = 0,
  var avoidDistraction  : Double = 0)

    extends EvaluateTarget {
  
  override def evaluate(intent:Intention, target:UnitInfo): Double = {
    List(
      weigh( inRange      (intent, target), preferInRange),
      weigh( value        (intent, target), preferValue),
      weigh( dps          (intent, target), preferDps),
      weigh( health       (intent, target), -avoidHealth),
      weigh( distance     (intent, target), -avoidDistance),
      weigh( distraction  (intent, target), -avoidDistraction)
    )
    .product
  }
  
  //TODO: Don't even calculate the value if the weight is 0
  //TODO: Consolidate with MovementProfile
  def weigh(value:Double, weight:Double):Double = if (weight == 0) 1 else Math.pow(normalize(value), weight)
  def unboolify(value:Boolean)                  = if (value) 2 else 1
  def normalize(value:Double)                   = Math.min(Math.max(0.01, value), 1000000)
  
  def inRange(intent:Intention, target:UnitInfo):Double = {
    unboolify(intent.unit.pixelsFromEdge(target) <= intent.unit.unitClass.maxAirGroundRange)
  }
  
  def value(intent:Intention, target:UnitInfo):Double = {
    intent.unit.unitClass.totalCost
  }
  
  def dps(intent:Intention, target:UnitInfo):Double = {
    Math.max(target.unitClass.groundDps, target.unitClass.airDps)
  }
  
  def health(intent:Intention, target:UnitInfo):Double = {
    target.totalHealth
  }
  
  def distance(intent:Intention, target:UnitInfo):Double = {
    Math.max(8, intent.unit.pixelsFromEdge(target) - intent.unit.unitClass.maxAirGroundRange)
  }
  
  def distraction(intent:Intention, target:UnitInfo):Double = {
    if (intent.destination.isEmpty) return 1
  
    unboolify(target.travelPixels(intent.destination.get) > intent.unit.travelPixels(intent.destination.get))
  }
}
