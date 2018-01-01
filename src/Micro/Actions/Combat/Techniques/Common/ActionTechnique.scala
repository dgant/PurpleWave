package Micro.Actions.Combat.Techniques.Common

import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

abstract class ActionTechnique extends Action {
  
  protected trait Activator { def apply(values: Seq[Double]): Option[Double] }
  protected object One  extends Activator { def apply(values: Seq[Double]): Option[Double] = Some(1.0) }
  protected object Min  extends Activator { def apply(values: Seq[Double]): Option[Double] = ByOption.min(values)  }
  protected object Max  extends Activator { def apply(values: Seq[Double]): Option[Double] = ByOption.max(values)  }
  protected object Mean extends Activator { def apply(values: Seq[Double]): Option[Double] = ByOption.mean(values) }
  protected object RMS  extends Activator { def apply(values: Seq[Double]): Option[Double] = ByOption.rms(values)  }
  
  val activator = Mean
  
  val applicabilityBase = 1.0
  def applicabilitySelf(unit: FriendlyUnitInfo): Double = 1.0
  def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = Some(1.0)
  
  def evaluate(unit: FriendlyUnitInfo): Double = {
    lazy val applicabilitiesOther     = unit.matchups.others.flatMap(applicabilityOther(unit, _))
    lazy val totalApplicabilityOther  = activator(applicabilitiesOther.map(PurpleMath.clampToOne)).getOrElse(0.0)
    lazy val totalApplicabilitySelf   = PurpleMath.clampToOne(applicabilitySelf(unit))
    lazy val output                   = PurpleMath.clampToOne(applicabilityBase) * totalApplicabilitySelf * totalApplicabilitySelf
    
    // Performance shortcuts
    if (applicabilityBase       <= 0.0) return 0.0
    if (totalApplicabilitySelf  <= 0.0) return 0.0
    
    output
  }
}
