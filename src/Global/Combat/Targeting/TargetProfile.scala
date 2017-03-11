package Global.Combat.Targeting
import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.UnitInfo

class TargetProfile(
  preferInRange     : Double = 1,
  preferValue       : Double = 1,
  preferFocus       : Double = 1,
  avoidHealth       : Double = 1,
  avoidDistance     : Double = 1)
    extends EvaluateTarget {
  
  override def evaluate(intent:Intention, target:UnitInfo): Double = {
    List(
      weigh( inRange  (intent, target), preferInRange),
      weigh( value    (intent, target), preferValue),
      weigh( focus    (intent, target), preferFocus),
      weigh( health   (intent, target), -avoidHealth),
      weigh( distance (intent, target), -avoidDistance)
    )
    .product
  }
  
  //TODO: Consolidate with MovementProfile
  def weigh(value:Double, weight:Double):Double = normalize(Math.pow(value, weight))
  def unboolify(value:Boolean)                  = if (value) 2 else 1
  def normalize(value:Double)                   = Math.min(Math.max(0.000001, value), 1000000)
  
  def inRange(intent:Intention, target:UnitInfo):Double = {
    unboolify(intent.unit.distanceFromEdge(target) <= intent.unit.range)
  }
  
  def value(intent:Intention, target:UnitInfo):Double = {
    intent.unit.totalCost
  }
  
  def focus(intent:Intention, target:UnitInfo):Double = {
    With.grids.friendlyGroundStrength.get(target.tileCenter)
  }
  
  def health(intent:Intention, target:UnitInfo):Double = {
    target.totalHealth
  }
  
  def distance(intent:Intention, target:UnitInfo):Double = {
    Math.max(0, intent.unit.distanceFromEdge(target) - intent.unit.range)
  }
}
