package Planning.Plans.Army

import Lifecycle.With
import Planning.Plans.Compound.{If, IfThenElse}
import Planning.Yolo

class ConsiderAttacking
  extends IfThenElse(
    new If(() =>
      Yolo.active
      || With.battles.global.estimationAbstract.weGainValue
      || With.battles.global.estimationAbstract.weSurvive),
    new Attack)