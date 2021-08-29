package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Planning.Predicate
import Planning.Predicates.Never

class ProtossHuntersFFA extends ProtossFFA {
  override def doExpand: Predicate = new Never
}
