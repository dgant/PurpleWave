package Global.Combat.Targeting

import Types.Intents.Intention
import Types.UnitInfo.UnitInfo

trait EvaluateTarget {
  def evaluate(intent:Intention, target:UnitInfo):Double
}
