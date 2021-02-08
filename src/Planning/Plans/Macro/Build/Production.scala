package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.Buildable
import Planning.Prioritized
import Planning.ResourceLocks.{LockCurrency, LockUnits}

abstract class Production extends Prioritized {
  def producerCurrencyLocks: Seq[LockCurrency]
  def producerUnitLocks: Seq[LockUnits]
  def producerInProgress: Boolean
  def buildable: Buildable
  def isComplete: Boolean
  def onUpdate(): Unit
  def onCompletion(): Unit = {}
  def update(): Unit = {
    onUpdate()
  }
  val frameCreated: Int = With.frame
}
