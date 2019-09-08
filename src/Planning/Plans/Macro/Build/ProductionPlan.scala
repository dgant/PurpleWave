package Planning.Plans.Macro.Build

import Macro.Buildables.Buildable
import Planning.Plan
import Planning.ResourceLocks.{LockCurrency, LockUnits}

abstract class ProductionPlan extends Plan {
  def producerCurrencyLocks: Seq[LockCurrency]
  def producerUnitLocks: Seq[LockUnits]
  def producerInProgress: Boolean
  def buildable: Buildable
}
